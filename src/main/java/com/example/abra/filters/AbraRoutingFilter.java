package com.example.abra.filters;

import com.example.abra.models.DomainModel;
import com.example.abra.models.EndpointModel;
import com.example.abra.models.TestModel;
import com.example.abra.models.VariantModel;
import com.example.abra.services.DomainModelService;
import com.example.abra.services.EndpointService;
import com.example.abra.services.TestModelService;
import com.example.abra.services.VariantModelService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class AbraRoutingFilter extends OncePerRequestFilter {

    private final DomainModelService domainModelService;
    private final TestModelService testModelService;
    private final VariantModelService variantModelService;
    private final EndpointService endpointService;

    @Value("${abra.admin.host}")
    private String adminHost;

    private static final String VARIANT_COOKIE_PREFIX = "abra_variant_";
    private static final int COOKIE_MAX_AGE_DAYS = 30;
    private static final int COOKIE_MAX_AGE_SECONDS = COOKIE_MAX_AGE_DAYS * 24 * 60 * 60;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return request.getServerName().equalsIgnoreCase(adminHost);
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String host = request.getServerName();
        if (host == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        if (path == null) {
            path = "";
        }

        DomainModel domain = domainModelService
            .findActiveByDomainHost(host)
            .orElse(null);

        if (domain != null) {
            String domainId = domain.getDomain_id();
            if (domainId == null) {
                filterChain.doFilter(request, response);
                return;
            }

            TestModel matchedTest = testModelService
                .findBestMatchingTest(domainId, path)
                .orElse(null);

            if (matchedTest != null) {
                String testId = matchedTest.getTest_id();
                if (testId == null) {
                    filterChain.doFilter(request, response);
                    return;
                }

                List<VariantModel> variants =
                    variantModelService.findAllVariantsByTestId(
                        testId
                    );

                // Filter only active variants
                List<VariantModel> activeVariants = variants.stream()
                    .filter(VariantModel::isActive)
                    .toList();

                if (activeVariants.isEmpty()) {
                    log.warn("No active variants for test: {}", matchedTest.getName());
                    response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "No active variants available");
                    return;
                }

                // Step 1: Check for existing variant cookie or choose new variant
                String cookieName = VARIANT_COOKIE_PREFIX + matchedTest.getTest_id();
                VariantModel selectedVariant = getVariantFromCookie(request, cookieName, activeVariants)
                    .orElseGet(() -> {
                        // No valid cookie, choose variant randomly based on weights
                        VariantModel newVariant = chooseVariant(activeVariants);
                        // Set cookie to persist variant selection for 30 days
                        setVariantCookie(response, cookieName, newVariant.getVariant_id());
                        return newVariant;
                    });

                // Step 2: Select a healthy endpoint using round-robin load balancing
                EndpointModel selectedEndpoint = endpointService.selectEndpoint(
                    selectedVariant.getVariant_id()
                );

                if (selectedEndpoint == null) {
                    log.warn("No healthy endpoints available for variant: {}", selectedVariant.getName());
                    response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "No healthy endpoints available");
                    return;
                }

                // Build the target URL preserving the path and query string
                String targetUrl = buildTargetUrl(selectedEndpoint.getUrl(), path, request.getQueryString());
                
                log.info("Routing request: domain={}, path={}, test={}, variant={}, endpoint={}",
                    domain.getHost(), path, matchedTest.getName(), 
                    selectedVariant.getName(), selectedEndpoint.getUrl());

                // Redirect to the selected endpoint
                response.sendRedirect(targetUrl);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private VariantModel chooseVariant(List<VariantModel> variants) {
        int weightSum = variants
            .stream()
            .mapToInt(VariantModel::getWeight)
            .sum();

        int random = ThreadLocalRandom.current().nextInt(weightSum);

        int cumulativeWeight = 0;
        for (VariantModel variant : variants) {
            cumulativeWeight += variant.getWeight();
            if (random < cumulativeWeight) {
                return variant;
            }
        }

        return variants.get(0);
    }

    private String buildTargetUrl(String baseUrl, String path, String queryString) {
        StringBuilder targetUrl = new StringBuilder(baseUrl);
        
        // Ensure no double slashes when concatenating
        if (baseUrl.endsWith("/") && path.startsWith("/")) {
            targetUrl.deleteCharAt(targetUrl.length() - 1);
        } else if (!baseUrl.endsWith("/") && !path.startsWith("/")) {
            targetUrl.append("/");
        }
        
        targetUrl.append(path);
        
        if (queryString != null && !queryString.isEmpty()) {
            targetUrl.append("?").append(queryString);
        }
        
        return targetUrl.toString();
    }

    /**
     * Try to get the variant from cookie if it exists and is still valid (active)
     */
    private Optional<VariantModel> getVariantFromCookie(
        HttpServletRequest request,
        String cookieName,
        List<VariantModel> activeVariants
    ) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
            .filter(cookie -> cookieName.equals(cookie.getName()))
            .findFirst()
            .flatMap(cookie -> {
                String variantId = cookie.getValue();
                // Find the variant if it's still active
                return activeVariants.stream()
                    .filter(v -> v.getVariant_id().equals(variantId))
                    .findFirst();
            });
    }

    /**
     * Set a cookie to persist the variant selection for 30 days
     */
    private void setVariantCookie(HttpServletResponse response, String cookieName, String variantId) {
        Cookie cookie = new Cookie(cookieName, variantId);
        cookie.setMaxAge(COOKIE_MAX_AGE_SECONDS);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        log.debug("Set variant cookie: {}={}", cookieName, variantId);
    }
}

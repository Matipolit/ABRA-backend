package com.example.abra.filters;

import com.example.abra.models.DomainModel;
import com.example.abra.models.TestModel;
import com.example.abra.models.VariantModel;
import com.example.abra.services.DomainModelService;
import com.example.abra.services.TestModelService;
import com.example.abra.services.VariantModelService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class AbraRoutingFilter extends OncePerRequestFilter {

    private final DomainModelService domainModelService;
    private final TestModelService testModelService;
    private final VariantModelService variantModelService;

    @Value("${abra.admin.host}")
    private String adminHost;

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

                VariantModel selectedVariant = chooseVariant(variants);
                response.setContentType("text/plain");
                response
                    .getWriter()
                    .write(
                        "Matched domain: " +
                            domain.getHost() +
                            ", Path: " +
                            path +
                            ", Matched test: " +
                            matchedTest.getName() +
                            " (subpath: " +
                            matchedTest.getSubpath() +
                            "), Selected variant: " +
                            selectedVariant.getName()
                    );
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

        int currentCheckedVariantIdx = 0;
        int leftInCurrentVariant = variants
            .get(currentCheckedVariantIdx)
            .getWeight();

        for (int i = 0; i < weightSum; i++) {
            if (i == random) {
                return variants.get(currentCheckedVariantIdx);
            }

            leftInCurrentVariant--;
            if (leftInCurrentVariant <= 0) {
                currentCheckedVariantIdx++;
                leftInCurrentVariant = variants
                    .get(currentCheckedVariantIdx)
                    .getWeight();
            }
        }

        return variants.get(0);
    }
}

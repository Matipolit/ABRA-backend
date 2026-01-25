package com.example.abra.filters;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.example.abra.models.DomainModel;
import com.example.abra.models.EndpointModel;
import com.example.abra.models.TestModel;
import com.example.abra.models.VariantModel;
import com.example.abra.services.DomainModelService;
import com.example.abra.services.EndpointService;
import com.example.abra.services.TestModelService;
import com.example.abra.services.VariantModelService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AbraRoutingFilterTest {

    @Mock
    private DomainModelService domainModelService;

    @Mock
    private TestModelService testModelService;

    @Mock
    private VariantModelService variantModelService;

    @Mock
    private EndpointService endpointService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @InjectMocks
    private AbraRoutingFilter filter;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(filter, "adminHost", "admin.abra.local");
    }

    @Test
    void doFilter_noDomainMatch_continuesChain() throws Exception {
        when(request.getServerName()).thenReturn("unknown.com");
        when(request.getRequestURI()).thenReturn("/");
        when(
            domainModelService.findActiveByDomainHost("unknown.com")
        ).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(testModelService);
    }

    @Test
    void doFilter_domainMatch_noTestMatch_continuesChain() throws Exception {
        DomainModel domain = new DomainModel();
        domain.setDomain_id("d1");
        domain.setHost("example.com");

        when(request.getServerName()).thenReturn("example.com");
        when(request.getRequestURI()).thenReturn("/page");
        when(
            domainModelService.findActiveByDomainHost("example.com")
        ).thenReturn(Optional.of(domain));
        when(testModelService.findBestMatchingTest("d1", "/page")).thenReturn(
            Optional.empty()
        );

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(variantModelService);
    }

    @Test
    void doFilter_matchFound_redirectsToEndpoint() throws Exception {
        DomainModel domain = new DomainModel();
        domain.setDomain_id("d1");

        TestModel test = new TestModel();
        test.setTest_id("t1");
        test.setName("Test1");

        VariantModel variant = new VariantModel();
        variant.setVariant_id("v1");
        variant.setName("V1");
        variant.setWeight(100);
        variant.setActive(true);

        EndpointModel endpoint = new EndpointModel();
        endpoint.setUrl("http://target.com");

        when(request.getServerName()).thenReturn("example.com");
        when(request.getRequestURI()).thenReturn("/path");
        when(
            domainModelService.findActiveByDomainHost("example.com")
        ).thenReturn(Optional.of(domain));
        when(testModelService.findBestMatchingTest("d1", "/path")).thenReturn(
            Optional.of(test)
        );
        when(variantModelService.findAllVariantsByTestId("t1")).thenReturn(
            List.of(variant)
        );
        when(endpointService.selectEndpoint("v1")).thenReturn(endpoint);

        filter.doFilterInternal(request, response, chain);

        verify(response).addCookie(any(Cookie.class));
        verify(response).sendRedirect("http://target.com/path");
        verifyNoInteractions(chain);
    }

    @Test
    void doFilter_usesCookieVariant_ifPresent() throws Exception {
        DomainModel domain = new DomainModel();
        domain.setDomain_id("d1");

        TestModel test = new TestModel();
        test.setTest_id("t1");

        VariantModel v1 = new VariantModel();
        v1.setVariant_id("v1");
        v1.setActive(true);

        VariantModel v2 = new VariantModel();
        v2.setVariant_id("v2");
        v2.setActive(true); // Should be selected from cookie

        EndpointModel endpoint = new EndpointModel();
        endpoint.setUrl("http://v2.com");

        Cookie cookie = new Cookie("abra_variant_t1", "v2");
        when(request.getCookies()).thenReturn(new Cookie[] { cookie });

        when(request.getServerName()).thenReturn("example.com");
        when(request.getRequestURI()).thenReturn("/");
        when(
            domainModelService.findActiveByDomainHost("example.com")
        ).thenReturn(Optional.of(domain));
        when(testModelService.findBestMatchingTest("d1", "/")).thenReturn(
            Optional.of(test)
        );
        when(variantModelService.findAllVariantsByTestId("t1")).thenReturn(
            List.of(v1, v2)
        );
        when(endpointService.selectEndpoint("v2")).thenReturn(endpoint);

        filter.doFilterInternal(request, response, chain);

        verify(endpointService).selectEndpoint("v2");
        verify(response).sendRedirect("http://v2.com/");
    }
}

package com.example.abra;

import com.example.abra.models.DomainModel;
import com.example.abra.models.TestModel;
import com.example.abra.services.DomainModelService;
import com.example.abra.services.TestModelService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@RequiredArgsConstructor
public class AbraApplication {

    private final DomainModelService domainModelService;
    private final TestModelService testModelService;

    public static void main(String[] args) {
        SpringApplication.run(AbraApplication.class, args);
    }

    @GetMapping("/**")
    public String root(HttpServletRequest request) {
        String host = request.getServerName();
        String path = request.getRequestURI();
        String scheme = request.getScheme();

        DomainModel domain = domainModelService
            .findActiveByDomainHost(host)
            .orElse(null);

        if (domain != null) {
            TestModel matchedTest = testModelService
                .findBestMatchingTest(domain.getDomain_id(), path)
                .orElse(null);

            if (matchedTest != null) {
                return (
                    "Matched domain: " +
                    domain.getHost() +
                    ", Path: " +
                    path +
                    ", Matched test: " +
                    matchedTest.getName() +
                    " (subpath: " +
                    matchedTest.getSubpath() +
                    ")"
                );
            } else {
                return (
                    "Domain matched: " +
                    domain.getHost() +
                    ", Path: " +
                    path +
                    ", but no test matched this path"
                );
            }
        } else {
            return "No active domain found for host: " + host;
        }
    }
}

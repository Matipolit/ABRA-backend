package com.example.abra.services;

import com.example.abra.models.TestModel;
import com.example.abra.repositories.TestModelRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TestModelService {

    private final TestModelRepository testModelRepository;

    public List<TestModel> findAllTests() {
        return testModelRepository.findAll();
    }

    public List<TestModel> findAllActiveTestsForDomainWithSubpath(
        String domainId,
        String subpath
    ) {
        return testModelRepository.findAllByDomainIdBySubpathAndIsActiveTrue(
            domainId,
            subpath
        );
    }

    /**
     * Finds the best matching test for a given domain and request path.
     * Prioritizes more specific paths:
     * 1. Exact match has highest priority
     * 2. Longest prefix match if no exact match
     *
     * @param domainId The domain ID
     * @param requestPath The requested path (e.g., "/cart/details")
     * @return Optional containing the best matching test, or empty if no match
     */
    public Optional<TestModel> findBestMatchingTest(
        String domainId,
        String requestPath
    ) {
        List<TestModel> allTests =
            testModelRepository.findAllByDomainIdAndIsActiveTrue(domainId);

        return allTests
            .stream()
            .filter(test -> {
                String subpath = test.getSubpath();
                if (subpath == null || subpath.isEmpty()) {
                    return false;
                }
                // Proper path matching (e.g., "/cart" matches "/cart/details" but not "/cartography")
                return (
                    requestPath.equals(subpath) ||
                    requestPath.startsWith(subpath + "/") ||
                    (subpath.equals("/") && !requestPath.isEmpty())
                );
            })
            // longest/most specific first
            .max(Comparator.comparingInt(test -> test.getSubpath().length()));
    }

    public Optional<TestModel> findByTestId(String id) {
        return testModelRepository.findById(id);
    }

    public TestModel addTest(TestModel testModel) {
        return testModelRepository.save(testModel);
    }

    public void updateTest(TestModel updated) {
        testModelRepository.save(updated);
    }

    public void deleteTestById(String id) {
        testModelRepository.deleteById(id);
    }
}

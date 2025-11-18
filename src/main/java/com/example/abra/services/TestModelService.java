package com.example.abra.services;

import com.example.abra.models.TestModel;
import com.example.abra.repositories.TestModelRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TestModelService {

    private final TestModelRepository testModelRepository;

    public List<TestModel> findAllTests() {
        return testModelRepository.findAll();
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

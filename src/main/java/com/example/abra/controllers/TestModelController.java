package com.example.abra.controllers;

import com.example.abra.models.TestModel;
import com.example.abra.services.TestModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestModelController {

    private final TestModelService testModelService;

    @GetMapping
    public ResponseEntity<List<TestModel>> getAll() {
        return ResponseEntity.ok(testModelService.findAllTests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestModel> getById(@PathVariable String id) {
        return testModelService.findByTestId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TestModel> create(@RequestBody TestModel testModel) {
        TestModel created = testModelService.addTest(testModel);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(
            @PathVariable String id,
            @RequestBody TestModel testModel
    ) {
        testModelService.updateTest(testModel);
        return ResponseEntity.ok("Hello world");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        testModelService.deleteTestById(id);
        return ResponseEntity.noContent().build();
    }
}

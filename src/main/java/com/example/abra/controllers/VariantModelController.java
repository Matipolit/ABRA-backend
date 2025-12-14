package com.example.abra.controllers;

import com.example.abra.models.TestModel;
import com.example.abra.models.VariantModel;
import com.example.abra.services.TestModelService;
import com.example.abra.services.VariantModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/variants")
@RequiredArgsConstructor
public class VariantModelController {

    private final VariantModelService variantModelService;
    private final TestModelService testModelService;

    @GetMapping
    public ResponseEntity<List<VariantModel>> getAll() {
        return ResponseEntity.ok(variantModelService.findAllVariants());
    }

    @GetMapping("/byTestId/{testId}")
    public ResponseEntity<List<VariantModel>> getAllByTestId(@PathVariable String testId) {
        return ResponseEntity.ok(variantModelService.findAllVariantsByTestId(testId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VariantModel> getById(@PathVariable String id) {
        return variantModelService.findByVariantId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/byTestIds")
    public ResponseEntity<List<VariantModel>> getAllByTestId(@RequestBody List<String> testIds) {
        List<VariantModel> variants = new ArrayList<>();
        for (String testId : testIds) {
            Optional<TestModel> test = testModelService.findByTestId(testId);
            test.ifPresent(testModel -> variants.addAll(testModel.getVariantModels()));
        }
        return ResponseEntity.ok(variants);
    }

    @PostMapping
    public ResponseEntity<VariantModel> create(@RequestBody VariantModel variantModel) {
        VariantModel created = variantModelService.addVariant(variantModel);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(
            @PathVariable String id,
            @RequestBody VariantModel variantModel
    ) {
        variantModel.setVariant_id(id);
        variantModelService.updateVariant(variantModel);
        return ResponseEntity.ok("Updated");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        variantModelService.deleteVariantById(id);
        return ResponseEntity.noContent().build();
    }
}

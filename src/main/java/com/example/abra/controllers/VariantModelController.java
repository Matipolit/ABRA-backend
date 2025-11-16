package com.example.abra.controllers;

import com.example.abra.models.VariantModel;
import com.example.abra.services.VariantModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/variants")
@RequiredArgsConstructor
public class VariantModelController {

    private final VariantModelService variantModelService;

    @GetMapping
    public ResponseEntity<List<VariantModel>> getAll() {
        return ResponseEntity.ok(variantModelService.findAllVariants());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VariantModel> getById(@PathVariable String id) {
        return variantModelService.findByVariantId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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
        variantModelService.updateVariant(variantModel);
        return ResponseEntity.ok("Updated");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        variantModelService.deleteVariantById(id);
        return ResponseEntity.noContent().build();
    }
}

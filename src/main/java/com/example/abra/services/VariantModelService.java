package com.example.abra.services;

import com.example.abra.models.VariantModel;
import com.example.abra.repositories.VariantModelRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VariantModelService {

    private final VariantModelRepository variantModelRepository;

    public List<VariantModel> findAllVariants() {
        return variantModelRepository.findAll();
    }

    public List<VariantModel> findAllVariantsByTestId(String testId) {
        return variantModelRepository.findAllByTestId(testId);
    }

    public Optional<VariantModel> findByVariantId(String id) {
        return variantModelRepository.findById(id);
    }

    public VariantModel addVariant(VariantModel variantModel) {
        return variantModelRepository.save(variantModel);
    }

    public void updateVariant(VariantModel updated) {
        variantModelRepository.save(updated);
    }

    public void deleteVariantById(String id) {
        variantModelRepository.deleteById(id);
    }
}

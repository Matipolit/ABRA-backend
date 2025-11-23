package com.example.abra.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.abra.models.VariantModel;
import com.example.abra.repositories.VariantModelRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VariantModelServiceTest {

    @Mock
    private VariantModelRepository repository;

    @InjectMocks
    private VariantModelService service;

    @Test
    void findAllVariants_returnsList() {
        VariantModel vm = new VariantModel();
        vm.setVariant_id("v1");
        vm.setName("Var");
        vm.setWeight(10);
        vm.setTestModel(null);
        vm.setEndpointModels(null);

        when(repository.findAll()).thenReturn(List.of(vm));

        List<VariantModel> result = service.findAllVariants();

        assertEquals(1, result.size());
        verify(repository).findAll();
    }

    @Test
    void findByVariantId_found() {
        VariantModel vm = new VariantModel();
        vm.setVariant_id("v1");
        when(repository.findById("v1")).thenReturn(Optional.of(vm));

        Optional<VariantModel> result = service.findByVariantId("v1");
        assertTrue(result.isPresent());
        assertEquals("v1", result.get().getVariant_id());
        verify(repository).findById("v1");
    }

    @Test
    void addVariant_saves() {
        VariantModel vm = new VariantModel();
        when(repository.save(vm)).thenReturn(vm);

        VariantModel saved = service.addVariant(vm);
        assertNotNull(saved);
        verify(repository).save(vm);
    }

    @Test
    void updateVariant_callsSave() {
        VariantModel vm = new VariantModel();
        service.updateVariant(vm);
        verify(repository).save(vm);
    }

    @Test
    void deleteVariantById_callsDelete() {
        service.deleteVariantById("v1");
        verify(repository).deleteById("v1");
    }
}

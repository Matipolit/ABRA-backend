package com.example.abra.services;

import com.example.abra.models.TestModel;
import com.example.abra.repositories.TestModelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.lang.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestModelServiceTest {

    @Mock
    private TestModelRepository repository;

    @InjectMocks
    private TestModelService service;

    private @NonNull TestModel sample = new TestModel();

    @BeforeEach
    void setUp() {
        sample = new TestModel();
        sample.setTest_id("id-1");
        sample.setName("TestName");
        sample.setDescription("Desc");
        sample.setVariantModels(null);
    }

    @Test
    void findAllTests_returnsList() {
        when(repository.findAll()).thenReturn(Arrays.asList(sample));

        List<TestModel> result = service.findAllTests();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void findByTestId_found() {
        when(repository.findById("id-1")).thenReturn(Optional.of(sample));

        Optional<TestModel> result = service.findByTestId("id-1");

        assertTrue(result.isPresent());
        assertEquals("id-1", result.get().getTest_id());
        verify(repository).findById("id-1");
    }

    @Test
    void findByTestId_notFound() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        Optional<TestModel> result = service.findByTestId("missing");

        assertFalse(result.isPresent());
        verify(repository).findById("missing");
    }

    @Test
    void addTest_savesAndReturns() {
        when(repository.save(sample)).thenReturn(sample);

        TestModel created = service.addTest(sample);

        assertNotNull(created);
        assertEquals("id-1", created.getTest_id());
        verify(repository).save(sample);
    }

    @Test
    void updateTest_callsSave() {
        service.updateTest(sample);
        verify(repository).save(sample);
    }

    @Test
    void deleteTestById_callsRepository() {
        service.deleteTestById("id-1");
        verify(repository).deleteById("id-1");
    }
}

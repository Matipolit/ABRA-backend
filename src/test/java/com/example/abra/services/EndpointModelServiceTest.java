package com.example.abra.services;

import com.example.abra.models.EndpointModel;
import com.example.abra.repositories.EndpointModelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EndpointModelServiceTest {

    @Mock
    private EndpointModelRepository repository;

    @InjectMocks
    private EndpointModelService service;

    @Test
    void findAllEndpoints_returnsList() {
        EndpointModel e = new EndpointModel();
        e.setUrl("/u");
        e.setDescription("d");
        e.setAlive(true);
        e.setVariantModel(null);

        when(repository.findAll()).thenReturn(List.of(e));

        List<EndpointModel> result = service.findAllEndpoints();
        assertEquals(1, result.size());
        verify(repository).findAll();
    }

    @Test
    void findByEndpointId_found() {
        EndpointModel e = new EndpointModel();
        e.setUrl("/u");
        when(repository.findById("/u")).thenReturn(Optional.of(e));

        Optional<EndpointModel> result = service.findByEndpointId("/u");
        assertTrue(result.isPresent());
        assertEquals("/u", result.get().getUrl());
        verify(repository).findById("/u");
    }

    @Test
    void addEndpoint_saves() {
        EndpointModel e = new EndpointModel();
        when(repository.save(e)).thenReturn(e);

        EndpointModel saved = service.addEndpoint(e);
        assertNotNull(saved);
        verify(repository).save(e);
    }

    @Test
    void updateEndpoint_callsSave() {
        EndpointModel e = new EndpointModel();
        service.updateEndpoint(e);
        verify(repository).save(e);
    }

    @Test
    void deleteEndpointById_callsDelete() {
        service.deleteEndpointById("/u");
        verify(repository).deleteById("/u");
    }
}

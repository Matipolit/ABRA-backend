package com.example.abra.services;

import com.example.abra.models.EndpointModel;
import com.example.abra.repositories.EndpointModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EndpointModelService {

    private final EndpointModelRepository endpointModelRepository;

    public List<EndpointModel> findAllEndpoints() {
        return endpointModelRepository.findAll();
    }

    public Optional<EndpointModel> findByEndpointId(@NonNull String id) {
        return endpointModelRepository.findById(id);
    }

    public EndpointModel addEndpoint(@NonNull EndpointModel endpointModel) {
        return endpointModelRepository.save(endpointModel);
    }

    public void updateEndpoint(@NonNull EndpointModel updated) {
        endpointModelRepository.save(updated);
    }

    public void deleteEndpointById(@NonNull String id) {
        endpointModelRepository.deleteById(id);
    }
}

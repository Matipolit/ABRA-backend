package com.example.abra.controllers;

import com.example.abra.models.EndpointModel;
import com.example.abra.services.EndpointModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/endpoints")
@RequiredArgsConstructor
public class EndpointModelController {

    private final EndpointModelService endpointModelService;

    @GetMapping
    public ResponseEntity<List<EndpointModel>> getAll() {
        return ResponseEntity.ok(endpointModelService.findAllEndpoints());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EndpointModel> getById(@PathVariable String id) {
        return endpointModelService.findByEndpointId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EndpointModel> create(@RequestBody EndpointModel endpointModel) {
        EndpointModel created = endpointModelService.addEndpoint(endpointModel);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable String id,
            @RequestBody EndpointModel endpointModel
    ) {
        endpointModelService.updateEndpoint(endpointModel);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        endpointModelService.deleteEndpointById(id);
        return ResponseEntity.noContent().build();
    }
}

package com.example.abra.controllers;

import com.example.abra.models.DomainModel;
import com.example.abra.repositories.DomainModelRepository;
import com.example.abra.services.DomainModelService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/domains")
@RequiredArgsConstructor
public class DomainModelController {


    private final DomainModelService domainModelService;

    @GetMapping
    public ResponseEntity<Iterable<DomainModel>> getAllDomains() {
        return ResponseEntity.ok(domainModelService.findAllDomains());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DomainModel> getDomainById(@PathVariable String id) {
        return domainModelService.findByDomainId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DomainModel> createDomain(@RequestBody DomainModel domainModel) {
        DomainModel created = domainModelService.addDomain(domainModel);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateDomain(
            @PathVariable String id,
            @RequestBody DomainModel domainModel
    ) {
        domainModelService.updateDomain(domainModel);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDomain(@PathVariable String id) {
        domainModelService.deleteDomainById(id);
        return ResponseEntity.noContent().build();
    }
}
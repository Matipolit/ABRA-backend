package com.example.abra.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.abra.models.DomainModel;
import com.example.abra.repositories.DomainModelRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DomainModelService {
    private final DomainModelRepository domainModelRepository;

    public List<DomainModel> findAllDomains() {
        return domainModelRepository.findAll();
    }

    public Optional<DomainModel> findByDomainId(String id) {
        return domainModelRepository.findById(id);
    }

    public DomainModel addDomain(DomainModel domainModel) {
        return domainModelRepository.save(domainModel);
    }

    public void updateDomain(DomainModel updated) {
        domainModelRepository.save(updated);
    }

    public void deleteDomainById(String id) {
        domainModelRepository.deleteById(id);
    }
    
}

package com.example.abra.services;

import com.example.abra.models.DomainModel;
import com.example.abra.repositories.DomainModelRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public Optional<DomainModel> findActiveByDomainHost(String host) {
        return domainModelRepository.findByHostAndIsActiveTrue(host);
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

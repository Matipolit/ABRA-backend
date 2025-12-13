package com.example.abra.services;

import com.example.abra.models.DomainModel;
import com.example.abra.repositories.DomainModelRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DomainModelService {

    private final DomainModelRepository domainModelRepository;

    public List<DomainModel> findAllDomains() {
        return domainModelRepository.findAll();
    }

    public Optional<DomainModel> findByDomainId(@NonNull String id) {
        return domainModelRepository.findById(id);
    }

    public Optional<DomainModel> findActiveByDomainHost(@NonNull String host) {
        return domainModelRepository.findByHostAndIsActiveTrue(host);
    }

    public DomainModel addDomain(@NonNull DomainModel domainModel) {
        return domainModelRepository.save(domainModel);
    }

    public void updateDomain(@NonNull DomainModel updated) {
        domainModelRepository.save(updated);
    }

    public void deleteDomainById(@NonNull String id) {
        domainModelRepository.deleteById(id);
    }
}

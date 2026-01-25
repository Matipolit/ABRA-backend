package com.example.abra.services;

import com.example.abra.models.DomainModel;
import com.example.abra.repositories.DomainModelRepository;
import jakarta.persistence.EntityNotFoundException;
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
class DomainModelServiceTest {

    @Mock
    private DomainModelRepository domainModelRepository;

    @InjectMocks
    private DomainModelService domainModelService;

    private DomainModel domain(String id, String host, boolean active) {
        DomainModel d = new DomainModel();
        d.setDomain_id(id);
        d.setHost(host);
        d.setActive(active);
        return d;
    }

    @Test
    void findAllDomains_shouldReturnAllDomains() {
        List<DomainModel> domains = List.of(
                domain("1", "example.com", true),
                domain("2", "test.com", false)
        );

        when(domainModelRepository.findAll()).thenReturn(domains);

        List<DomainModel> result = domainModelService.findAllDomains();

        assertEquals(2, result.size());
        verify(domainModelRepository).findAll();
    }

    @Test
    void findByDomainId_whenExists_shouldReturnDomain() {
        DomainModel domain = domain("1", "example.com", true);
        when(domainModelRepository.findById("1"))
                .thenReturn(Optional.of(domain));

        Optional<DomainModel> result =
                domainModelService.findByDomainId("1");

        assertTrue(result.isPresent());
        assertEquals("example.com", result.get().getHost());
        assertTrue(result.get().isActive());
    }

    @Test
    void findByDomainId_whenNotExists_shouldReturnEmpty() {
        when(domainModelRepository.findById("404"))
                .thenReturn(Optional.empty());

        Optional<DomainModel> result =
                domainModelService.findByDomainId("404");

        assertTrue(result.isEmpty());
    }

    @Test
    void findActiveByDomainHost_shouldReturnOnlyActiveDomain() {
        DomainModel domain = domain("1", "example.com", true);

        when(domainModelRepository.findByHostAndIsActiveTrue("example.com"))
                .thenReturn(Optional.of(domain));

        Optional<DomainModel> result =
                domainModelService.findActiveByDomainHost("example.com");

        assertTrue(result.isPresent());
        assertTrue(result.get().isActive());
        assertEquals("example.com", result.get().getHost());
    }

    @Test
    void addDomain_shouldSaveAndReturnDomain() {
        DomainModel domain = domain(null, "example.com", true);

        when(domainModelRepository.save(domain)).thenReturn(domain);

        DomainModel saved = domainModelService.addDomain(domain);

        assertNotNull(saved);
        verify(domainModelRepository).save(domain);
    }

    @Test
    void updateDomain_whenExists_shouldUpdateHostAndActive() {
        DomainModel existing = domain("1", "old.com", false);
        DomainModel updated  = domain("1", "new.com", true);

        when(domainModelRepository.findById("1"))
                .thenReturn(Optional.of(existing));

        domainModelService.updateDomain(updated);

        assertEquals("new.com", existing.getHost());
        assertTrue(existing.isActive());
        verify(domainModelRepository).save(existing);
    }

    @Test
    void updateDomain_whenNotExists_shouldThrowException() {
        DomainModel updated = domain("404", "x.com", true);

        when(domainModelRepository.findById("404"))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> domainModelService.updateDomain(updated));

        verify(domainModelRepository, never()).save(any());
    }

    @Test
    void deleteDomainById_shouldInvokeRepository() {
        domainModelService.deleteDomainById("1");

        verify(domainModelRepository).deleteById("1");
    }
}

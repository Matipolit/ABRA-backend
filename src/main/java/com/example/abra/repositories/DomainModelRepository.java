package com.example.abra.repositories;

import com.example.abra.models.DomainModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DomainModelRepository
    extends JpaRepository<DomainModel, String> {
    @Query("SELECT d FROM domain d WHERE d.host= :host AND d.active = true")
    Optional<DomainModel> findByHostAndIsActiveTrue(@Param("host") String host);
}

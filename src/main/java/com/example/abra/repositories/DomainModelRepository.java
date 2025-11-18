package com.example.abra.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.abra.models.DomainModel;

@Repository
public interface DomainModelRepository extends JpaRepository<DomainModel, String> {
}
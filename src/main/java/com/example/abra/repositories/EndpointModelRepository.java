package com.example.abra.repositories;

import com.example.abra.models.EndpointModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EndpointModelRepository extends JpaRepository<EndpointModel, String> {
}

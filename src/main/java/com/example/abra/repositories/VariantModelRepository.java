package com.example.abra.repositories;

import com.example.abra.models.VariantModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VariantModelRepository extends JpaRepository<VariantModel, String> {
}

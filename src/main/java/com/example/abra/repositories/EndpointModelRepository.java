package com.example.abra.repositories;

import com.example.abra.models.EndpointModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EndpointModelRepository extends JpaRepository<EndpointModel, String> {
    
    @Query("SELECT e FROM endpoint e WHERE e.variantModel.variant_id = :variantId")
    List<EndpointModel> findByVariantId(String variantId);
}

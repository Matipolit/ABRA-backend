package com.example.abra.repositories;

import com.example.abra.models.VariantModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VariantModelRepository
    extends JpaRepository<VariantModel, String> {
    @Query(
        "SELECT v FROM variant v WHERE v.testModel.test_id = :testId AND v.active = true"
    )
    List<VariantModel> findAllByTestId(@Param("testId") String testId);
}

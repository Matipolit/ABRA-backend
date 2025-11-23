package com.example.abra.repositories;

import com.example.abra.models.TestModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TestModelRepository extends JpaRepository<TestModel, String> {
    @Query(
        "SELECT t FROM test t WHERE t.domainModel.domain_id = :domainId AND t.subpath = :subpath AND t.active = true"
    )
    List<TestModel> findAllByDomainIdBySubpathAndIsActiveTrue(
        @Param("domainId") String domainId,
        @Param("subpath") String subpath
    );

    @Query(
        "SELECT t FROM test t WHERE t.domainModel.domain_id = :domainId AND t.active = true"
    )
    List<TestModel> findAllByDomainIdAndIsActiveTrue(
        @Param("domainId") String domainId
    );
}

package com.example.abra.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity(name = "test")
public class TestModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String test_id;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "is_active")
    private boolean active;

    @Column(name = "subpath", length = 100)
    private String subpath;

    @Column(name = "description", length = 500)
    private String description;

    @OneToMany(
        cascade = CascadeType.ALL,
        fetch = FetchType.EAGER,
        mappedBy = "testModel"
    )
    @JsonIgnoreProperties({ "testModel", "endpointModels" })
    private List<VariantModel> variantModels;

    @ManyToOne
    @JoinColumn(name = "domain_id")
    @JsonIgnoreProperties({ "defaultEndpoints", "tests" })
    private DomainModel domainModel;
}

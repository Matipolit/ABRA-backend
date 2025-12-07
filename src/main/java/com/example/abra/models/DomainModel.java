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
@Entity(name = "domain")
public class DomainModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String domain_id;

    @Column(name = "is_active")
    private boolean active;

    @Column(name = "host", unique = true)
    private String host;

    @OneToMany(
        mappedBy = "domainModel",
        cascade = CascadeType.ALL,
        fetch = FetchType.EAGER
    )
    @JsonIgnoreProperties({ "domainModel", "variantModel" })
    private List<EndpointModel> defaultEndpoints;

    @OneToMany(
        mappedBy = "domainModel",
        cascade = CascadeType.ALL,
        fetch = FetchType.EAGER
    )
    @JsonIgnoreProperties({ "domainModel", "variantModels" })
    private List<TestModel> tests;
}

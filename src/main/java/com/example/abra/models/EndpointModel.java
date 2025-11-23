package com.example.abra.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity(name = "endpoint")
public class EndpointModel {

    @Id
    @Column(name = "url", length = 50)
    private String url;

    @Column(name = "is_active")
    private boolean active;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "alive")
    private boolean alive;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    @JsonIgnoreProperties({ "testModel", "endpointModels" })
    private VariantModel variantModel;

    @ManyToOne
    @JoinColumn(name = "domain_id")
    @JsonIgnoreProperties({ "defaultEndpoints", "tests" })
    private DomainModel domainModel;
}

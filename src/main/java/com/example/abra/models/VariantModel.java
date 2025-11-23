package com.example.abra.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity(name = "variant")
public class VariantModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String variant_id;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "is_active")
    private boolean active;

    @Column(name = "description", length = 500)
    private String description;

    @Min(1)
    @Max(100)
    @Column(name = "weight")
    private int weight;

    @ManyToOne
    @JoinColumn(name = "test_id")
    @JsonIgnoreProperties({ "variantModels", "domainModel" })
    private TestModel testModel;

    @OneToMany(
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY,
        mappedBy = "variantModel"
    )
    @JsonIgnoreProperties({ "variantModel", "domainModel" })
    private List<EndpointModel> endpointModels = new ArrayList<>();
}

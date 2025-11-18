package com.example.abra.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity(name="variant")
public class VariantModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String variant_id;
    @Column(name="name", length = 50)
    private String name;
    @Column(name="weight")
    private int weight;
    @ManyToOne()
    @JoinColumn(name="test_id")
    private TestModel testModel;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "variantModel")
    private List<EndpointModel> endpointModels = new ArrayList<>();
}

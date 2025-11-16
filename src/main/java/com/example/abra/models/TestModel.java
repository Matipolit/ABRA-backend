package com.example.abra.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity(name="test")
public class TestModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String test_id;
    @Column(name="name", length = 50)
    private String name;
    @Column(name="description", length = 50)
    private String description;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "testModel")
    private List<VariantModel> variantModels;
}

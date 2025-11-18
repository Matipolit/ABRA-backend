package com.example.abra.models;

import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity(name="domain")
public class DomainModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String domain_id;
    
    @Column(name = "is_active")
    private boolean is_active;

    @OneToMany(mappedBy = "domainModel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EndpointModel> defaultEndpoints;

    @OneToMany(mappedBy = "domainModel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TestModel> tests;
}

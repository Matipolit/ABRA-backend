package com.example.abra.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity(name="endpoint")
public class EndpointModel {
    @Id
    @Column(name="url", length = 50)
    private String url;
    @Column(name="description", length = 50)
    private String description;
    @Column(name="alive")
    private boolean alive;
    @ManyToOne()
    @JoinColumn(name="variant_id")
    private VariantModel variantModel;
}

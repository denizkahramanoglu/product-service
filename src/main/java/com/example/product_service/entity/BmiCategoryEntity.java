package com.example.product_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bmi_categories")
@Getter
@Setter
public class BmiCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "min_bmi")
    private double minBmi;

    @Column(name = "max_bmi")
    private double maxBmi;
}
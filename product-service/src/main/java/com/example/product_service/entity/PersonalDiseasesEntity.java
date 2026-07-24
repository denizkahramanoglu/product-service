package com.example.product_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "personal_diseases")
@Getter
@Setter
public class PersonalDiseasesEntity {
    @Id
    private Long id;

    @Column(name = "disease_name")
    private String diseaseName;

    @Column(name = "code")
    private String code; // risk_parameters ile bağlandığımız alan
}
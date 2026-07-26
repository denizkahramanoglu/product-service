package com.example.product_service.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "occupations")
@Getter
@Setter
public class OccupationEntity {
    @Id
    private Long id;

    @Column(name = "occupation_name")
    private String occupationName;

    @Column(name = "code")
    private String code; // risk_parameters ile bağlandığımız alan
}
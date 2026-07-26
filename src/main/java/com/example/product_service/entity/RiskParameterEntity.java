package com.example.product_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "risk_parameters")
@Getter
@Setter
public class RiskParameterEntity { // İsmini kendi entity adına göre ayarlayabilirsin (RiskParameter de olabilir)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "value")
    private BigDecimal value;
}
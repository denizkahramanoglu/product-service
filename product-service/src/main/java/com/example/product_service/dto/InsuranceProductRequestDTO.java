package com.example.product_service.dto;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceProductRequestDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String name;
    private BigDecimal price;

}
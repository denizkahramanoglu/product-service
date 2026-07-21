package com.example.product_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LifePremiumResponseDTO {
    private Long calculatedPremium;
    private String currency;
    private String message;
}
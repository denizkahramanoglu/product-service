package com.example.product_service.dto;

import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CurrencyDTO {
    private String code;
    private String definition;
}
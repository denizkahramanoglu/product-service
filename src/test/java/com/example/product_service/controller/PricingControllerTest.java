package com.example.product_service.controller;

import com.example.product_service.dto.PricingRequestDTO;
import com.example.product_service.dto.PricingResponseDTO;
import com.example.product_service.service.PricingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricingControllerTest {

    @Mock
    private PricingService pricingService;

    @InjectMocks
    private PricingController pricingController;

    @Test
    @DisplayName("calculatePrice - Fiyat hesaplama servisi çağrılmalı ve 200 OK ile dönmeli")
    void calculatePrice_shouldReturnCalculatedPriceAndOk() {
        PricingResponseDTO mockResponse = new PricingResponseDTO();
        when(pricingService.calculateFinalPrice(any(PricingRequestDTO.class))).thenReturn(mockResponse);

        assertEquals(ResponseEntity.ok(mockResponse),
                pricingController.calculatePrice(new PricingRequestDTO()));
    }
}
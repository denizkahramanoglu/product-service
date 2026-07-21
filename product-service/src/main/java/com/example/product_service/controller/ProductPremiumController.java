package com.example.product_service.controller;

import com.example.product_service.dto.LifePremiumRequestDTO;
import com.example.product_service.dto.LifePremiumResponseDTO;
import com.example.product_service.service.PremiumCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/premium")
@RequiredArgsConstructor
public class ProductPremiumController {

    private final PremiumCalculationService premiumCalculationService;

    @Operation(summary = "Müşteriye özgü sigorta fiyatlandırması")
    @PostMapping("/calculate")
    public ResponseEntity<LifePremiumResponseDTO> getPrice(@RequestBody LifePremiumRequestDTO request) {
        return ResponseEntity.ok(premiumCalculationService.calculateLifeInsurancePremium(request));
    }
}
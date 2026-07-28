package com.example.product_service.controller;

import com.example.product_service.dto.PricingRequestDTO;
import com.example.product_service.dto.PricingResponseDTO;
import com.example.product_service.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final PricingService pricingService;

    @PostMapping("/calculate")
    public ResponseEntity<PricingResponseDTO> calculatePrice(@RequestBody PricingRequestDTO request) {
        PricingResponseDTO response = pricingService.calculateFinalPrice(request);
        return ResponseEntity.ok(response);
    }

}
package com.example.product_service.controller;

import com.example.product_service.dto.InsuranceProductRequestDTO;
import com.example.product_service.dto.InsuranceProductResponseDTO;
import com.example.product_service.entity.InsuranceProductEntity;
import com.example.product_service.service.InsuranceProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class InsuranceProductController {

    private final InsuranceProductService insuranceProductService;

    @PostMapping
    public ResponseEntity<InsuranceProductResponseDTO> createProduct(@RequestBody InsuranceProductRequestDTO requestDto) {
        // Controller sadece servisi çağırır, gerisine karışmaz
        return ResponseEntity.ok(insuranceProductService.saveProduct(requestDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InsuranceProductResponseDTO> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(insuranceProductService.getProductById(id));
    }
}
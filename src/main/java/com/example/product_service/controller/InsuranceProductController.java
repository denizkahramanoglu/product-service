package com.example.product_service.controller;

import com.example.product_service.dto.InsuranceProductRequestDTO;
import com.example.product_service.dto.InsuranceProductResponseDTO;
import com.example.product_service.service.InsuranceProductService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class InsuranceProductController {

    private final InsuranceProductService insuranceProductService;

    @Operation(summary = "ürünü oluşturma")
    @PostMapping
    public ResponseEntity<InsuranceProductResponseDTO> createProduct(@RequestBody InsuranceProductRequestDTO requestDto) {
        InsuranceProductResponseDTO response = insuranceProductService.saveProduct(requestDto);


        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @Operation(summary = "Gerekli ürünü getirme")
    @GetMapping("/{id}")
    public ResponseEntity<InsuranceProductResponseDTO> getProduct(@PathVariable Long id) {


        return ResponseEntity.ok(insuranceProductService.getProductById(id));
    }

    @Operation(summary = "Ürünü güncelleme")
    @PutMapping("/{id}")
    public ResponseEntity<InsuranceProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @RequestBody InsuranceProductRequestDTO requestDto) {

        InsuranceProductResponseDTO response = insuranceProductService.updateProduct(id, requestDto);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Ürünü silme (Soft Delete)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        insuranceProductService.deleteProduct(id);


        return ResponseEntity.noContent().build();
    }
}
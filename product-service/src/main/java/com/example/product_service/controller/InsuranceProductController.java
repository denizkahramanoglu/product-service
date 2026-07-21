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

        // Yeni kaynak (ürün) oluşturulduğu için 201 döndürüyoruz
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @Operation(summary = "Gerekli ürünü getirme")
    @GetMapping("/{id}")
    public ResponseEntity<InsuranceProductResponseDTO> getProduct(@PathVariable Long id) {

        // Eğer ID yoksa, Service katmanı anında BusinessException fırlatacak.
        // Hata yakalayıcı da onu 404 (Not Found) JSON formatına çevirip kullanıcıya basacak.
        return ResponseEntity.ok(insuranceProductService.getProductById(id));
    }
}
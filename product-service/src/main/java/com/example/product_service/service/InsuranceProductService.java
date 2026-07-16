package com.example.product_service.service;

import com.example.product_service.dto.InsuranceProductRequestDTO;
import com.example.product_service.dto.InsuranceProductResponseDTO;
import com.example.product_service.entity.InsuranceProductEntity;
import com.example.product_service.repository.InsuranceProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InsuranceProductService {

    private final InsuranceProductRepository repository;

    public InsuranceProductResponseDTO saveProduct(InsuranceProductRequestDTO requestDto) {
        // 1. Entity'yi oluştur (Builder'da productId yerine name kullan)
        InsuranceProductEntity entity = InsuranceProductEntity.builder()
                .name(requestDto.getName()) // İsim ismi almalı
                .price(requestDto.getPrice())
                .build();

        // 2. Veritabanına kaydet (DB otomatik id verecek)
        InsuranceProductEntity savedEntity = repository.save(entity);

        // 3. Kayıtlı veriyi DTO olarak dön (Burada id olarak savedEntity.getId() kullanılır)
        return InsuranceProductResponseDTO.builder()
                .productId(savedEntity.getProductId())
                .name(savedEntity.getName())
                .price(savedEntity.getPrice())
                .build();
    }

    public InsuranceProductResponseDTO getProductById(Long id) {
        InsuranceProductEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı: " + id));

        return InsuranceProductResponseDTO.builder()
                .productId(entity.getProductId())
                .name(entity.getName())
                .price(entity.getPrice())
                .build();
    }
}
package com.example.product_service.service;

import com.example.product_service.dto.InsuranceProductRequestDTO;
import com.example.product_service.dto.InsuranceProductResponseDTO;
import com.example.product_service.entity.InsuranceProductEntity;
import com.example.product_service.exception.BusinessException;
import com.example.product_service.repository.InsuranceProductRepository;
import com.example.product_service.mapper.InsuranceProductMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;


@Service
@RequiredArgsConstructor
public class InsuranceProductService {

    // Bu sınıf için loglayıcıyı tanımlıyoruz
    private static final Logger logger = LoggerFactory.getLogger(InsuranceProductService.class);

    private final InsuranceProductRepository repository;
    private final InsuranceProductMapper mapper;

    public InsuranceProductResponseDTO saveProduct(InsuranceProductRequestDTO requestDto) {


        InsuranceProductEntity entity = mapper.toEntity(requestDto);
        InsuranceProductEntity savedEntity = repository.save(entity);

        // Ürün kaydedildiğinde konsola bilgi (info) logu düşebilirsin
        logger.info("Yeni sigorta ürünü başarıyla kaydedildi. Ürün ID: {}", savedEntity.getProductId());

        return mapper.toResponseDto(savedEntity);
    }

    public InsuranceProductResponseDTO getProductById(Long id) {

        // RuntimeException yerine 404 dönen kurumsal hatamızı fırlatıyoruz
        InsuranceProductEntity entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Ürün bulunamadı! Geçersiz Ürün ID: " + id,
                        HttpStatus.NOT_FOUND
                ));

        return mapper.toResponseDto(entity);
    }
}
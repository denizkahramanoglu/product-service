package com.example.product_service.service;

import com.example.product_service.client.ParameterClient;
import com.example.product_service.dto.CurrencyResponseDTO;
import com.example.product_service.dto.LifePremiumRequestDTO;
import com.example.product_service.dto.LifePremiumResponseDTO;
import com.example.product_service.entity.InsuranceProductEntity;
import com.example.product_service.repository.InsuranceProductRepository;
import com.example.product_service.util.PricingEngine;
import com.example.product_service.entity.OccupationEntity;
import com.example.product_service.exception.BusinessException;
import com.example.product_service.repository.OccupationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class PremiumCalculationService {

    private static final Logger logger = LoggerFactory.getLogger(PremiumCalculationService.class);

    private final InsuranceProductRepository insuranceProductRepository;
    private final OccupationRepository occupationRepository;
    private final PricingEngine pricingEngine;
    private final ParameterClient parameterClient; // Feign Client'ımız

    public LifePremiumResponseDTO calculateLifeInsurancePremium(LifePremiumRequestDTO request) {

        InsuranceProductEntity product = insuranceProductRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException("Ürün bulunamadı! ID: " + request.getProductId(), HttpStatus.NOT_FOUND));

        BigDecimal premium = product.getPrice();

        OccupationEntity occupation = occupationRepository.findById(request.getOccupationId())
                .orElseThrow(() -> new BusinessException("Meslek bulunamadı! ID: " + request.getOccupationId(), HttpStatus.NOT_FOUND));

        // ... Çarpan hesaplamaları aynı şekilde kalıyor ...
        premium = premium.multiply(pricingEngine.getOccupationFactor(occupation.getRiskLevel()));
        premium = premium.multiply(pricingEngine.getAgeFactor(request.getAge()));
        premium = premium.multiply(pricingEngine.getGenderFactor(request.getGender()));
        premium = premium.multiply(pricingEngine.getSmokerFactor(request.isSmoker()));
        premium = premium.multiply(pricingEngine.getChronicDiseaseFactor(request.isHasChronicDisease()));
        premium = premium.multiply(pricingEngine.getBmiFactor(request.getHeightInMeters(), request.getWeightInKg()));

        //VERİ DOĞRULAMA (VALIDATION)
        String finalCurrency = "TRY";
        String requestedCurrency = request.getRequestedCurrency();

        if (requestedCurrency != null && !requestedCurrency.isBlank() && !requestedCurrency.equalsIgnoreCase("TRY")) {
            finalCurrency = requestedCurrency.toUpperCase();

            try {
                CurrencyResponseDTO currencyDto = parameterClient.getByCode(finalCurrency);

                if (currencyDto == null || currencyDto.getCode() == null) {
                    throw new BusinessException("Sistemde tanımlı olmayan para birimi: " + finalCurrency, HttpStatus.BAD_REQUEST);
                }

            } catch (feign.FeignException.NotFound e) {
                throw new BusinessException("Sistemde tanımlı olmayan para birimi: " + finalCurrency, HttpStatus.BAD_REQUEST);
            } catch (Exception e) {
                throw new BusinessException("Para birimi doğrulaması yapılamadı (Parameter servisine ulaşılamıyor).", HttpStatus.SERVICE_UNAVAILABLE);
            }
        }

        long finalPremium = premium.setScale(0, RoundingMode.HALF_UP).longValue();

        logger.info("Prim hesaplandı: {} {}. Ürün ID: {}", finalPremium, finalCurrency, request.getProductId());

        return LifePremiumResponseDTO.builder()
                .calculatedPremium(finalPremium)
                .currency(finalCurrency)
                .message("Hesaplama başarıyla tamamlandı.")
                .build();
    }
}
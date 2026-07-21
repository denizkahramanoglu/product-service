package com.example.product_service.service;

import com.example.product_service.client.ParameterClient;
import com.example.product_service.dto.CurrencyResponseDTO;
import com.example.product_service.dto.LifePremiumRequestDTO;
import com.example.product_service.dto.LifePremiumResponseDTO;
import com.example.product_service.entity.InsuranceProductEntity;
import com.example.product_service.entity.OccupationEntity;
import com.example.product_service.exception.BusinessException;
import com.example.product_service.repository.InsuranceProductRepository;
import com.example.product_service.repository.OccupationRepository;
import com.example.product_service.util.PricingEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PremiumCalculationServiceTest {

    @Mock
    private InsuranceProductRepository insuranceProductRepository;

    @Mock
    private OccupationRepository occupationRepository;

    @Mock
    private PricingEngine pricingEngine;

    @Mock
    private ParameterClient parameterClient;

    @InjectMocks
    private PremiumCalculationService premiumCalculationService;

    @Test
    void calculateLifeInsurancePremium_Success_WithCustomCurrency() {
        LifePremiumRequestDTO request = new LifePremiumRequestDTO();
        request.setProductId(10L);
        request.setOccupationId(1L);
        request.setAge(30);
        request.setRequestedCurrency("EUR"); // Özel para birimi testi

        InsuranceProductEntity product = new InsuranceProductEntity();
        product.setProductId(10L);
        product.setPrice(new BigDecimal("5000.00"));

        when(insuranceProductRepository.findById(10L)).thenReturn(Optional.of(product));

        OccupationEntity occupation = new OccupationEntity();
        when(occupationRepository.findById(1L)).thenReturn(Optional.of(occupation));

        // ParameterClient mock davranışı: EUR sisteme kayıtlı döner
        CurrencyResponseDTO currencyResponse = new CurrencyResponseDTO();
        currencyResponse.setCode("EUR");
        currencyResponse.setDefinition("Euro");
        when(parameterClient.getByCode("EUR")).thenReturn(currencyResponse);

        when(pricingEngine.getOccupationFactor(any())).thenReturn(new BigDecimal("1.5"));
        when(pricingEngine.getAgeFactor(30)).thenReturn(BigDecimal.ONE);
        when(pricingEngine.getGenderFactor(any())).thenReturn(BigDecimal.ONE);
        when(pricingEngine.getSmokerFactor(anyBoolean())).thenReturn(BigDecimal.ONE);
        when(pricingEngine.getChronicDiseaseFactor(anyBoolean())).thenReturn(BigDecimal.ONE);
        when(pricingEngine.getBmiFactor(anyDouble(), anyDouble())).thenReturn(BigDecimal.ONE);

        LifePremiumResponseDTO response = premiumCalculationService.calculateLifeInsurancePremium(request);

        assertNotNull(response);
        assertEquals(7500L, response.getCalculatedPremium());
        assertEquals("EUR", response.getCurrency());

        // ParameterClient'ın gerçekten çağrıldığını doğrula
        verify(parameterClient, times(1)).getByCode("EUR");
    }

    @Test
    void calculateLifeInsurancePremium_Success_DefaultCurrencyTRY() {
        LifePremiumRequestDTO request = new LifePremiumRequestDTO();
        request.setProductId(10L);
        request.setOccupationId(2L);
        request.setRequestedCurrency("TRY"); // TRY için Parameter servisine gitmemesi gerekir

        InsuranceProductEntity product = new InsuranceProductEntity();
        product.setProductId(10L);
        product.setPrice(new BigDecimal("5000.00"));

        when(insuranceProductRepository.findById(10L)).thenReturn(Optional.of(product));

        OccupationEntity occupation = new OccupationEntity();
        when(occupationRepository.findById(2L)).thenReturn(Optional.of(occupation));

        when(pricingEngine.getOccupationFactor(any())).thenReturn(BigDecimal.ONE);
        when(pricingEngine.getAgeFactor(anyInt())).thenReturn(BigDecimal.ONE);
        when(pricingEngine.getGenderFactor(any())).thenReturn(BigDecimal.ONE);
        when(pricingEngine.getSmokerFactor(anyBoolean())).thenReturn(BigDecimal.ONE);
        when(pricingEngine.getChronicDiseaseFactor(anyBoolean())).thenReturn(BigDecimal.ONE);
        when(pricingEngine.getBmiFactor(anyDouble(), anyDouble())).thenReturn(BigDecimal.ONE);

        LifePremiumResponseDTO response = premiumCalculationService.calculateLifeInsurancePremium(request);

        assertNotNull(response);
        assertEquals(5000L, response.getCalculatedPremium());
        assertEquals("TRY", response.getCurrency());

        // TRY seçildiğinde Parameter servisi hiç çağrılmamalıdır
        verifyNoInteractions(parameterClient);
    }

    @Test
    void calculateLifeInsurancePremium_InvalidCurrency_ThrowsBusinessException() {
        LifePremiumRequestDTO request = new LifePremiumRequestDTO();
        request.setProductId(10L);
        request.setOccupationId(1L);
        request.setRequestedCurrency("XYZ"); // Veritabanında olmayan geçersiz bir para birimi

        InsuranceProductEntity product = new InsuranceProductEntity();
        product.setProductId(10L);
        product.setPrice(new BigDecimal("5000.00"));

        when(insuranceProductRepository.findById(10L)).thenReturn(Optional.of(product));

        OccupationEntity occupation = new OccupationEntity();
        when(occupationRepository.findById(1L)).thenReturn(Optional.of(occupation));

        when(pricingEngine.getOccupationFactor(any())).thenReturn(BigDecimal.ONE);
        when(pricingEngine.getAgeFactor(anyInt())).thenReturn(BigDecimal.ONE);
        when(pricingEngine.getGenderFactor(any())).thenReturn(BigDecimal.ONE);
        when(pricingEngine.getSmokerFactor(anyBoolean())).thenReturn(BigDecimal.ONE);
        when(pricingEngine.getChronicDiseaseFactor(anyBoolean())).thenReturn(BigDecimal.ONE);
        when(pricingEngine.getBmiFactor(anyDouble(), anyDouble())).thenReturn(BigDecimal.ONE);

        // ParameterClient geçersiz bir birim için null dönerse
        when(parameterClient.getByCode("XYZ")).thenThrow(feign.FeignException.NotFound.class);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                premiumCalculationService.calculateLifeInsurancePremium(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getMessage().contains("Sistemde tanımlı olmayan para birimi"));
    }

    @Test
    void calculateLifeInsurancePremium_ProductNotFound_ThrowsBusinessException() {
        LifePremiumRequestDTO request = new LifePremiumRequestDTO();
        request.setProductId(99L);

        when(insuranceProductRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                premiumCalculationService.calculateLifeInsurancePremium(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertTrue(exception.getMessage().contains("Ürün bulunamadı"));

        verifyNoInteractions(occupationRepository);
        verifyNoInteractions(pricingEngine);
        verifyNoInteractions(parameterClient);
    }

    @Test
    void calculateLifeInsurancePremium_OccupationNotFound_ThrowsBusinessException() {
        LifePremiumRequestDTO request = new LifePremiumRequestDTO();
        request.setProductId(10L);
        request.setOccupationId(99L);

        InsuranceProductEntity product = new InsuranceProductEntity();
        product.setProductId(10L);
        product.setPrice(new BigDecimal("5000.00"));

        when(insuranceProductRepository.findById(10L)).thenReturn(Optional.of(product));
        when(occupationRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                premiumCalculationService.calculateLifeInsurancePremium(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertTrue(exception.getMessage().contains("Meslek bulunamadı"));

        verifyNoInteractions(pricingEngine);
        verifyNoInteractions(parameterClient);
    }
}
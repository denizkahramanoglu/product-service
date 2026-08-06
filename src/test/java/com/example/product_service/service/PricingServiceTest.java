package com.example.product_service.service;

import com.example.product_service.dto.PricingRequestDTO;
import com.example.product_service.dto.PricingResponseDTO;
import com.example.product_service.entity.InsuranceProductEntity;
import com.example.product_service.exception.BusinessException;
import com.example.product_service.repository.InsuranceProductRepository;
import com.example.product_service.repository.RiskParameterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock
    private InsuranceProductRepository productRepository;

    @Mock
    private RiskParameterRepository riskParameterRepository;

    @InjectMocks
    private PricingService pricingService;

    private PricingRequestDTO validRequest;
    private InsuranceProductEntity mockProduct;

    @BeforeEach
    void setUp() {
        validRequest = new PricingRequestDTO();
        validRequest.setProductId(1L);
        validRequest.setAge(30);
        validRequest.setGender("MALE");
        validRequest.setWeight(75.0);
        validRequest.setHeight(180.0);
        validRequest.setOccupationId(10L);
        validRequest.setSmoker(true);
        validRequest.setPersonalDiseaseIds(Arrays.asList(100L, 101L));

        mockProduct = new InsuranceProductEntity();
        mockProduct.setProductId(1L);
        mockProduct.setBasePrice(BigDecimal.valueOf(1000));
        mockProduct.setCode("USD");
    }

    @Test
    void testCalculateFinalPrice_Success_WithDiseasesAndSmoker() {
        // Mocking Product
        when(productRepository.findById(validRequest.getProductId())).thenReturn(Optional.of(mockProduct));

        // Mocking Multipliers
        when(riskParameterRepository.findValueByCode("AGE_30")).thenReturn(BigDecimal.valueOf(1.2));
        when(riskParameterRepository.findValueByCode("GENDER_MALE")).thenReturn(BigDecimal.valueOf(1.1));
        when(riskParameterRepository.findBmiRiskValue(75.0, 180.0)).thenReturn(BigDecimal.valueOf(1.05));
        when(riskParameterRepository.findOccupationRiskValue(10L)).thenReturn(BigDecimal.valueOf(1.3));
        when(riskParameterRepository.findValueByCode("SMOKER_YES")).thenReturn(BigDecimal.valueOf(1.5));

        List<BigDecimal> mockDiseaseMultipliers = Arrays.asList(
                null,
                BigDecimal.valueOf(1.0),
                BigDecimal.valueOf(1.5),
                BigDecimal.valueOf(1.2)
        );
        when(riskParameterRepository.findDiseaseRiskValues(validRequest.getPersonalDiseaseIds()))
                .thenReturn(mockDiseaseMultipliers);

        // Execute
        PricingResponseDTO response = pricingService.calculateFinalPrice(validRequest);

        BigDecimal expectedFinalPrice = BigDecimal.valueOf(1000)
                .multiply(BigDecimal.valueOf(1.2))
                .multiply(BigDecimal.valueOf(1.1))
                .multiply(BigDecimal.valueOf(1.05))
                .multiply(BigDecimal.valueOf(1.3))
                .multiply(BigDecimal.valueOf(1.5))
                .multiply(BigDecimal.valueOf(1.5));

        assertNotNull(response);
        assertEquals(0, expectedFinalPrice.compareTo(response.getFinalPrice()));
        assertEquals("USD", response.getCurrency());
    }

    @Test
    void testCalculateFinalPrice_Success_NoDiseasesAndNonSmoker() {
        validRequest.setSmoker(false);
        validRequest.setPersonalDiseaseIds(null);

        when(productRepository.findById(validRequest.getProductId())).thenReturn(Optional.of(mockProduct));
        when(riskParameterRepository.findValueByCode("AGE_30")).thenReturn(BigDecimal.valueOf(1.2));
        when(riskParameterRepository.findValueByCode("GENDER_MALE")).thenReturn(BigDecimal.valueOf(1.1));
        when(riskParameterRepository.findBmiRiskValue(75.0, 180.0)).thenReturn(BigDecimal.valueOf(1.05));
        when(riskParameterRepository.findOccupationRiskValue(10L)).thenReturn(BigDecimal.valueOf(1.3));
        when(riskParameterRepository.findValueByCode("SMOKER_NO")).thenReturn(BigDecimal.valueOf(1.0));

        PricingResponseDTO response = pricingService.calculateFinalPrice(validRequest);

        BigDecimal expectedFinalPrice = BigDecimal.valueOf(1000)
                .multiply(BigDecimal.valueOf(1.2))
                .multiply(BigDecimal.valueOf(1.1))
                .multiply(BigDecimal.valueOf(1.05))
                .multiply(BigDecimal.valueOf(1.3))
                .multiply(BigDecimal.valueOf(1.0))
                .multiply(BigDecimal.ONE);

        assertNotNull(response);
        assertEquals(0, expectedFinalPrice.compareTo(response.getFinalPrice()));
    }

    @Test
    void testCalculateFinalPrice_ThrowsException_ProductNotFound() {
        when(productRepository.findById(validRequest.getProductId())).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> pricingService.calculateFinalPrice(validRequest));
        assertTrue(exception.getMessage().contains("Sigorta ürünü bulunamadı"));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void testCalculateFinalPrice_ThrowsException_AgeParameterNotFound() {
        when(productRepository.findById(validRequest.getProductId())).thenReturn(Optional.of(mockProduct));
        when(riskParameterRepository.findValueByCode("AGE_30")).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> pricingService.calculateFinalPrice(validRequest));
        assertTrue(exception.getMessage().contains("Yaş grubu için risk parametresi bulunamadı"));
    }

    @Test
    void testCalculateFinalPrice_ThrowsException_GenderParameterNotFound() {
        when(productRepository.findById(validRequest.getProductId())).thenReturn(Optional.of(mockProduct));
        when(riskParameterRepository.findValueByCode("AGE_30")).thenReturn(BigDecimal.ONE);
        when(riskParameterRepository.findValueByCode("GENDER_MALE")).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> pricingService.calculateFinalPrice(validRequest));
        assertTrue(exception.getMessage().contains("Cinsiyet için risk parametresi bulunamadı"));
    }

    @Test
    void testCalculateFinalPrice_ThrowsException_BmiParameterNotFound() {
        when(productRepository.findById(validRequest.getProductId())).thenReturn(Optional.of(mockProduct));
        when(riskParameterRepository.findValueByCode(anyString())).thenReturn(BigDecimal.ONE);
        when(riskParameterRepository.findBmiRiskValue(anyDouble(), anyDouble())).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> pricingService.calculateFinalPrice(validRequest));
        assertTrue(exception.getMessage().contains("uygun BMI aralığı bulunamadı"));
    }

    @Test
    void testCalculateFinalPrice_ThrowsException_OccupationParameterNotFound() {
        when(productRepository.findById(validRequest.getProductId())).thenReturn(Optional.of(mockProduct));
        when(riskParameterRepository.findValueByCode("AGE_30")).thenReturn(BigDecimal.ONE);
        when(riskParameterRepository.findValueByCode("GENDER_MALE")).thenReturn(BigDecimal.ONE);
        when(riskParameterRepository.findBmiRiskValue(anyDouble(), anyDouble())).thenReturn(BigDecimal.ONE);
        when(riskParameterRepository.findOccupationRiskValue(anyLong())).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> pricingService.calculateFinalPrice(validRequest));
        assertTrue(exception.getMessage().contains("Meslek bilgisi veya meslek risk parametresi bulunamadı"));
    }

    @Test
    void testCalculateFinalPrice_ThrowsException_SmokerParameterNotFound() {
        when(productRepository.findById(validRequest.getProductId())).thenReturn(Optional.of(mockProduct));
        when(riskParameterRepository.findValueByCode("AGE_30")).thenReturn(BigDecimal.ONE);
        when(riskParameterRepository.findValueByCode("GENDER_MALE")).thenReturn(BigDecimal.ONE);
        when(riskParameterRepository.findBmiRiskValue(anyDouble(), anyDouble())).thenReturn(BigDecimal.ONE);
        when(riskParameterRepository.findOccupationRiskValue(anyLong())).thenReturn(BigDecimal.ONE);
        when(riskParameterRepository.findValueByCode("SMOKER_YES")).thenReturn(null); // Smoker exception branch

        BusinessException exception = assertThrows(BusinessException.class, () -> pricingService.calculateFinalPrice(validRequest));
        assertTrue(exception.getMessage().contains("Sigara kullanım durumu için risk parametresi bulunamadı"));
    }

    @Test
    void testCalculateFinalPrice_ThrowsException_DiseaseParametersEmpty() {
        when(productRepository.findById(validRequest.getProductId())).thenReturn(Optional.of(mockProduct));
        when(riskParameterRepository.findValueByCode("AGE_30")).thenReturn(BigDecimal.ONE);
        when(riskParameterRepository.findValueByCode("GENDER_MALE")).thenReturn(BigDecimal.ONE);
        when(riskParameterRepository.findBmiRiskValue(anyDouble(), anyDouble())).thenReturn(BigDecimal.ONE);
        when(riskParameterRepository.findOccupationRiskValue(anyLong())).thenReturn(BigDecimal.ONE);
        when(riskParameterRepository.findValueByCode("SMOKER_YES")).thenReturn(BigDecimal.ONE);

        // Hastalık listesi dolu ama DB'den boş liste dönüyor durumu
        when(riskParameterRepository.findDiseaseRiskValues(validRequest.getPersonalDiseaseIds()))
                .thenReturn(Collections.emptyList());

        BusinessException exception = assertThrows(BusinessException.class, () -> pricingService.calculateFinalPrice(validRequest));
        assertTrue(exception.getMessage().contains("Belirtilen hastalıklara ait risk parametreleri sistemde bulunamadı"));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
    @Test
    void testCalculateFinalPrice_Success_EmptyDiseaseList() {
        validRequest.setPersonalDiseaseIds(Collections.emptyList());

        when(productRepository.findById(validRequest.getProductId())).thenReturn(Optional.of(mockProduct));
        when(riskParameterRepository.findValueByCode("AGE_30")).thenReturn(BigDecimal.valueOf(1.2));
        when(riskParameterRepository.findValueByCode("GENDER_MALE")).thenReturn(BigDecimal.valueOf(1.1));
        when(riskParameterRepository.findBmiRiskValue(75.0, 180.0)).thenReturn(BigDecimal.valueOf(1.05));
        when(riskParameterRepository.findOccupationRiskValue(10L)).thenReturn(BigDecimal.valueOf(1.3));
        when(riskParameterRepository.findValueByCode("SMOKER_YES")).thenReturn(BigDecimal.valueOf(1.5));

        PricingResponseDTO response = pricingService.calculateFinalPrice(validRequest);

        BigDecimal expectedFinalPrice = BigDecimal.valueOf(1000)
                .multiply(BigDecimal.valueOf(1.2))
                .multiply(BigDecimal.valueOf(1.1))
                .multiply(BigDecimal.valueOf(1.05))
                .multiply(BigDecimal.valueOf(1.3))
                .multiply(BigDecimal.valueOf(1.5))
                .multiply(BigDecimal.ONE);

        assertNotNull(response);
        assertEquals(0, expectedFinalPrice.compareTo(response.getFinalPrice()));

        verify(riskParameterRepository, never()).findDiseaseRiskValues(any());
    }
}
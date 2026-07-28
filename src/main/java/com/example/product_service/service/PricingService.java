package com.example.product_service.service;

import com.example.product_service.dto.PricingRequestDTO;
import com.example.product_service.dto.PricingResponseDTO;
import com.example.product_service.entity.InsuranceProductEntity;
import com.example.product_service.enums.SmokerStatus;
import com.example.product_service.exception.BusinessException;
import com.example.product_service.repository.InsuranceProductRepository;
import com.example.product_service.repository.RiskParameterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static com.example.product_service.util.BusinessExceptionUtil.businessExceptionCheckerAndThrowException;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final InsuranceProductRepository productRepository;
    private final RiskParameterRepository riskParameterRepository;

    public PricingResponseDTO calculateFinalPrice(PricingRequestDTO request) {

        InsuranceProductEntity product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException("Sigorta ürünü bulunamadı. ID: " + request.getProductId(), HttpStatus.NOT_FOUND));

        BigDecimal basePrice = product.getBasePrice();
        String currency = product.getCode();

        BigDecimal ageMultiplier = riskParameterRepository.findValueByCode("AGE_" + request.getAge());
        businessExceptionCheckerAndThrowException(ageMultiplier == null, "Yaş grubu için risk parametresi bulunamadı. Yaş: " + request.getAge(), HttpStatus.NOT_FOUND);
        BigDecimal genderMultiplier = riskParameterRepository.findValueByCode("GENDER_" + request.getGender().toUpperCase());
        businessExceptionCheckerAndThrowException(genderMultiplier == null ,"Cinsiyet için risk parametresi bulunamadı: " + request.getGender(), HttpStatus.NOT_FOUND);
        BigDecimal bmiMultiplier = riskParameterRepository.findBmiRiskValue(request.getWeight(), request.getHeight());
        businessExceptionCheckerAndThrowException(bmiMultiplier == null , "Girilen boy ve kilo değerlerine uygun BMI aralığı bulunamadı.", HttpStatus.NOT_FOUND);
        BigDecimal occupationMultiplier = riskParameterRepository.findOccupationRiskValue(request.getOccupationId());
        businessExceptionCheckerAndThrowException(occupationMultiplier == null , "Meslek bilgisi veya meslek risk parametresi bulunamadı. ID: " + request.getOccupationId(), HttpStatus.NOT_FOUND);
        SmokerStatus smokerStatus = SmokerStatus.fromBoolean(request.isSmoker());
        BigDecimal smokerMultiplier = riskParameterRepository.findValueByCode(smokerStatus.name());
        businessExceptionCheckerAndThrowException(smokerMultiplier == null , "Sigara kullanım durumu için risk parametresi bulunamadı.", HttpStatus.NOT_FOUND);
        BigDecimal diseaseMultiplier = calculateDiseaseMultiplier(request.getPersonalDiseaseIds());

        BigDecimal finalPrice = basePrice
                .multiply(ageMultiplier)
                .multiply(genderMultiplier)
                .multiply(bmiMultiplier)
                .multiply(occupationMultiplier)
                .multiply(smokerMultiplier)
                .multiply(diseaseMultiplier);

        return new PricingResponseDTO(finalPrice, currency);
    }

    private BigDecimal calculateDiseaseMultiplier(List<Long> diseaseIds) {
        if (CollectionUtils.isEmpty(diseaseIds)) {
            return BigDecimal.ONE;
        }

        List<BigDecimal> diseaseValues = riskParameterRepository.findDiseaseRiskValues(diseaseIds);
        businessExceptionCheckerAndThrowException(CollectionUtils.isEmpty(diseaseValues), "Belirtilen hastalıklara ait risk parametreleri sistemde bulunamadı.", HttpStatus.NOT_FOUND);

        return diseaseValues.stream()
                .filter(Objects::nonNull)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ONE);
    }
}
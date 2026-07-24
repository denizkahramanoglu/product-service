package com.example.product_service.service;

import com.example.product_service.dto.PricingRequestDTO;
import com.example.product_service.dto.PricingResponseDTO;
import com.example.product_service.entity.InsuranceProductEntity;
import com.example.product_service.exception.BusinessException;
import com.example.product_service.repository.InsuranceProductRepository;
import com.example.product_service.repository.RiskParameterRepository;
import com.example.product_service.util.BusinessExceptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

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
        BusinessExceptionUtil.businessExceptionCheckerAndThrowException(ageMultiplier == null, "Yaş grubu için risk parametresi bulunamadı. Yaş: " + request.getAge(), HttpStatus.NOT_FOUND);

        BigDecimal genderMultiplier = riskParameterRepository.findValueByCode("GENDER_" + request.getGender().toUpperCase());
        BusinessExceptionUtil.businessExceptionCheckerAndThrowException(genderMultiplier==null ,"Cinsiyet için risk parametresi bulunamadı: " + request.getGender(), HttpStatus.NOT_FOUND);


        BigDecimal bmiMultiplier = riskParameterRepository.findBmiRiskValue(request.getWeight(), request.getHeight());
        BusinessExceptionUtil.businessExceptionCheckerAndThrowException(bmiMultiplier==null , "Girilen boy ve kilo değerlerine uygun BMI aralığı bulunamadı.", HttpStatus.NOT_FOUND);


        BigDecimal occupationMultiplier = riskParameterRepository.findOccupationRiskValue(request.getOccupationId());
        BusinessExceptionUtil.businessExceptionCheckerAndThrowException(occupationMultiplier == null , "Meslek bilgisi veya meslek risk parametresi bulunamadı. ID: " + request.getOccupationId(), HttpStatus.NOT_FOUND);

        String smokerCode = request.isSmoker() ? "SMOKER_YES" : "SMOKER_NO";
        BigDecimal smokerMultiplier = riskParameterRepository.findValueByCode(smokerCode);
        BusinessExceptionUtil.businessExceptionCheckerAndThrowException(smokerMultiplier == null , "Sigara kullanım durumu için risk parametresi bulunamadı.", HttpStatus.NOT_FOUND);


        BigDecimal diseaseMultiplier = BigDecimal.ONE;
        if (request.getPersonalDiseaseIds() != null && !request.getPersonalDiseaseIds().isEmpty()) {
            List<BigDecimal> diseaseValues = riskParameterRepository.findDiseaseRiskValues(request.getPersonalDiseaseIds());
            if (diseaseValues.isEmpty()) {
                throw new BusinessException("Belirtilen hastalıklara ait risk parametreleri sistemde bulunamadı.", HttpStatus.NOT_FOUND);
            }
            for (BigDecimal dVal : diseaseValues) {
                if (dVal != null && dVal.compareTo(diseaseMultiplier) > 0) {
                    diseaseMultiplier = dVal;
                }
            }
        }

        BigDecimal finalPrice = basePrice
                .multiply(ageMultiplier)
                .multiply(genderMultiplier)
                .multiply(bmiMultiplier)
                .multiply(occupationMultiplier)
                .multiply(smokerMultiplier)
                .multiply(diseaseMultiplier);

        return new PricingResponseDTO(finalPrice, currency);
    }

}
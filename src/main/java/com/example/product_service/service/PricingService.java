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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.example.product_service.util.BusinessExceptionUtil.businessExceptionCheckerAndThrowException;

/**
 * Sigorta ürünleri için fiyatlandırma hesaplamalarını gerçekleştiren servis sınıfı.
 * Yaş, cinsiyet, boy/kilo (BMI), meslek, sigara kullanımı ve mevcut hastalıklar gibi
 * çeşitli risk parametrelerini değerlendirerek poliçenin nihai fiyatını belirler.
 *
 * @author deniz
 */
@Service
@RequiredArgsConstructor
public class PricingService {

    private final InsuranceProductRepository productRepository;
    private final RiskParameterRepository riskParameterRepository;

    /**
     * Müşteriden alınan parametrelere göre sigorta ürününün nihai fiyatını hesaplar.
     * Taban fiyat üzerinden yaş, cinsiyet, BMI, meslek, sigara kullanımı ve hastalık
     * durumlarına ait risk çarpanlarını uygulayarak toplam tutarı bulur.
     *
     * @param request Fiyat hesaplaması için gerekli müşteri ve ürün bilgilerini içeren {@link PricingRequestDTO} nesnesi
     * @return Hesaplanmış nihai fiyatı ve para birimini içeren {@link PricingResponseDTO} nesnesi
     * @throws BusinessException Sigorta ürünü bulunamazsa veya ilgili risk parametrelerinden herhangi biri eksikse fırlatılır
     */
    public PricingResponseDTO calculateFinalPrice(PricingRequestDTO request) {

        InsuranceProductEntity product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException("Sigorta ürünü bulunamadı. ID: " + request.getProductId(), HttpStatus.NOT_FOUND));

        BigDecimal basePrice = product.getBasePrice();
        String currency = product.getCode();

        BigDecimal ageMultiplier = riskParameterRepository.findValueByCode("AGE_" + request.getAge());
        businessExceptionCheckerAndThrowException(ageMultiplier == null, "Yaş grubu için risk parametresi bulunamadı. Yaş: " + request.getAge(), HttpStatus.NOT_FOUND);

        BigDecimal genderMultiplier = riskParameterRepository.findValueByCode("GENDER_" + request.getGender().toUpperCase());
        businessExceptionCheckerAndThrowException(genderMultiplier == null, "Cinsiyet için risk parametresi bulunamadı: " + request.getGender(), HttpStatus.NOT_FOUND);

        BigDecimal bmiMultiplier = riskParameterRepository.findBmiRiskValue(request.getWeight(), request.getHeight());
        businessExceptionCheckerAndThrowException(bmiMultiplier == null, "Girilen boy ve kilo değerlerine uygun BMI aralığı bulunamadı.", HttpStatus.NOT_FOUND);

        BigDecimal occupationMultiplier = riskParameterRepository.findOccupationRiskValue(request.getOccupationId());
        businessExceptionCheckerAndThrowException(occupationMultiplier == null, "Meslek bilgisi veya meslek risk parametresi bulunamadı. ID: " + request.getOccupationId(), HttpStatus.NOT_FOUND);

        SmokerStatus smokerStatus = SmokerStatus.fromBoolean(request.isSmoker());
        BigDecimal smokerMultiplier = riskParameterRepository.findValueByCode(smokerStatus.name());
        businessExceptionCheckerAndThrowException(smokerMultiplier == null, "Sigara kullanım durumu için risk parametresi bulunamadı.", HttpStatus.NOT_FOUND);

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

    /**
     * Müşterinin sahip olduğu hastalıkların risk çarpanlarını hesaplar.
     * Birden fazla hastalık olması durumunda, riski en yüksek olan hastalığın çarpanı baz alınır.
     * Hiçbir hastalık yoksa etkisiz eleman olan 1 değeri dönülür.
     *
     * @param diseaseIds Müşteriye ait hastalıkların veritabanındaki benzersiz kimlik (ID) listesi
     * @return Hastalıklar arasından en yüksek risk çarpanı değeri (Hastalık yoksa {@link BigDecimal#ONE})
     * @throws BusinessException Belirtilen hastalıklara ait risk değerleri sistemde bulunamazsa fırlatılır
     */
    private BigDecimal calculateDiseaseMultiplier(List<Long> diseaseIds) {

        if (CollectionUtils.isEmpty(diseaseIds)) {
            return BigDecimal.ONE;
        }

        List<BigDecimal> diseaseValues = Optional.ofNullable(riskParameterRepository.findDiseaseRiskValues(diseaseIds)).orElse(Collections.emptyList());
        businessExceptionCheckerAndThrowException(diseaseValues.isEmpty(), "Belirtilen hastalıklara ait risk parametreleri sistemde bulunamadı.", HttpStatus.NOT_FOUND);

        return diseaseValues.stream()
                .filter(Objects::nonNull)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ONE);
    }
}
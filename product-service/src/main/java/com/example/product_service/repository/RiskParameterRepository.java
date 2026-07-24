package com.example.product_service.repository;

import com.example.product_service.entity.RiskParameterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;


@Repository
public interface RiskParameterRepository extends JpaRepository<RiskParameterEntity, Long> {
    @Query("SELECT rp.value FROM RiskParameterEntity rp WHERE rp.code = :code")
    BigDecimal findValueByCode(@Param("code") String code);

    // 2. BMI risk katsayısı (BMI tablosu ile JOIN)
    @Query("SELECT rp.value FROM BmiCategoryEntity bc " +
            "JOIN RiskParameterEntity rp ON bc.code = rp.code " +
            "WHERE (:weight / (:height * :height)) BETWEEN bc.minBmi AND bc.maxBmi")
    BigDecimal findBmiRiskValue(@Param("weight") double weight, @Param("height") double height);

    // 3. Meslek risk katsayısı (Meslek tablosu ile Risk tablosunu JOIN'ler)
    @Query("SELECT rp.value FROM OccupationEntity o " +
            "JOIN RiskParameterEntity rp ON o.code = rp.code " +
            "WHERE o.id = :occupationId")
    BigDecimal findOccupationRiskValue(@Param("occupationId") Long occupationId);

    // 4. Hastalık risk katsayıları (Seçilen hastalık ID listesine göre tüm risk değerlerini çeker)
    @Query("SELECT rp.value FROM PersonalDiseasesEntity pd " +
            "JOIN RiskParameterEntity rp ON pd.code = rp.code " +
            "WHERE pd.id IN :diseaseIds")
    List<BigDecimal> findDiseaseRiskValues(@Param("diseaseIds") List<Long> diseaseIds);
}

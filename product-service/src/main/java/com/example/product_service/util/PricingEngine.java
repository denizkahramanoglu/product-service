package com.example.product_service.util;

import com.example.product_service.enums.Gender;
import com.example.product_service.enums.OccupationRisk;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class PricingEngine {

    public BigDecimal getOccupationFactor(OccupationRisk risk) {
        return switch (risk) {
            case LOW -> new BigDecimal("1.00");
            case MEDIUM -> new BigDecimal("1.15");
            case HIGH -> new BigDecimal("1.40");
            case EXTREME -> new BigDecimal("2.00");
        };
    }

    public BigDecimal getAgeFactor(int age) {
        if (age < 18) return new BigDecimal("1.00");
        if (age <= 30) return new BigDecimal("1.00");
        if (age <= 45) return new BigDecimal("1.20");
        if (age <= 60) return new BigDecimal("1.50");
        if (age <= 75) return new BigDecimal("2.20");
        return new BigDecimal("3.00");
    }

    public BigDecimal getGenderFactor(Gender gender) {
        return (gender == Gender.MALE) ? new BigDecimal("1.05") : new BigDecimal("1.00");
    }
    public BigDecimal getSmokerFactor(boolean isSmoker) {
        return isSmoker ? new BigDecimal("1.50") : BigDecimal.ONE;
    }
    public BigDecimal getChronicDiseaseFactor(boolean hasChronicDisease) {
        return hasChronicDisease ? new BigDecimal("1.50") : BigDecimal.ONE;
    }
    public BigDecimal getBmiFactor(double heightInMeters, double weightInKg) {
        double bmi = weightInKg / (heightInMeters * heightInMeters);
        if (heightInMeters <= 0) return BigDecimal.ONE;

        if (bmi < 18.5) return new BigDecimal("1.10"); // Zayıf (Riskli)
        if (bmi < 25) return new BigDecimal("1.00");   // İdeal
        if (bmi < 30) return new BigDecimal("1.15");   // Hafif Kilolu
        if (bmi < 35) return new BigDecimal("1.30");   // Obez (Tip 1)
        return new BigDecimal("1.60");                 // İleri Obez (Yüksek Risk)
    }
}
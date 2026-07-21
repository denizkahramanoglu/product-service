package com.example.product_service.dto;


import com.example.product_service.enums.Gender;
import lombok.Data;

@Data
public class LifePremiumRequestDTO {
    // Temel Bilgiler
    private int age;
    private Gender gender;

    // Sağlık ve Fiziksel Bilgiler
    private double heightInMeters;
    private double weightInKg;
    private boolean isSmoker;
    private boolean hasPriorSurgery;
    private boolean hasChronicDisease;
    private boolean hasFamilyHistoryOfCriticalIllness;

    // Meslek Bilgisi
    private Long occupationId;

    private String requestedCurrency;
    private long productId;
}
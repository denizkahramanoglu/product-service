package com.example.product_service.enums;

public enum SmokerStatus {
    SMOKER_YES,
    SMOKER_NO;
public static SmokerStatus fromBoolean(boolean isSmoker) {
    return isSmoker ? SMOKER_YES : SMOKER_NO;
}
}
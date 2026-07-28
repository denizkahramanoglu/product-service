package com.example.product_service.util;

import com.example.product_service.exception.BusinessException;
import org.springframework.http.HttpStatus;


public final class BusinessExceptionUtil {

    private BusinessExceptionUtil() {
    }

    public static void businessExceptionCheckerAndThrowException(boolean condition, String exceptionMessage, HttpStatus httpStatus) {
        if (condition) {
            throw new BusinessException(exceptionMessage, httpStatus);
        }
    }
}
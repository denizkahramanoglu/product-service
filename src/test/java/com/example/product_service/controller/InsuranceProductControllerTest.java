package com.example.product_service.controller;

import com.example.product_service.dto.InsuranceProductRequestDTO;
import com.example.product_service.dto.InsuranceProductResponseDTO;
import com.example.product_service.service.InsuranceProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InsuranceProductControllerTest {

    @Mock
    private InsuranceProductService insuranceProductService;

    @InjectMocks
    private InsuranceProductController insuranceProductController;

    @Test
    @DisplayName("createProduct - Servis çağrılmalı ve 201 CREATED statüsü ile oluşturulan ürün dönmeli")
    void createProduct_shouldReturnCreatedProduct() {
        InsuranceProductResponseDTO mockResponse = new InsuranceProductResponseDTO();
        when(insuranceProductService.saveProduct(any(InsuranceProductRequestDTO.class))).thenReturn(mockResponse);

        assertEquals(ResponseEntity.status(HttpStatus.CREATED).body(mockResponse),
                insuranceProductController.createProduct(new InsuranceProductRequestDTO()));
    }

    @Test
    @DisplayName("getProduct - İlgili ID ile servis çağrılmalı ve 200 OK statüsü ile ürün dönmeli")
    void getProduct_shouldReturnProductWithOk() {
        InsuranceProductResponseDTO mockResponse = new InsuranceProductResponseDTO();
        when(insuranceProductService.getProductById(1L)).thenReturn(mockResponse);

        assertEquals(ResponseEntity.ok(mockResponse),
                insuranceProductController.getProduct(1L));
    }

    @Test
    @DisplayName("updateProduct - İlgili ID ile servis çağrılmalı ve 200 OK statüsü ile güncellenen ürün dönmeli")
    void updateProduct_shouldReturnUpdatedProductWithOk() {
        InsuranceProductResponseDTO mockResponse = new InsuranceProductResponseDTO();
        when(insuranceProductService.updateProduct(eq(1L), any(InsuranceProductRequestDTO.class))).thenReturn(mockResponse);

        assertEquals(ResponseEntity.ok(mockResponse),
                insuranceProductController.updateProduct(1L, new InsuranceProductRequestDTO()));
    }

    @Test
    @DisplayName("deleteProduct - Servisin silme metodu tetiklenmeli ve 204 NO_CONTENT dönmeli")
    void deleteProduct_shouldCallServiceAndReturnNoContent() {
        assertEquals(ResponseEntity.noContent().build(),
                insuranceProductController.deleteProduct(1L));

        verify(insuranceProductService).deleteProduct(1L);
    }
}
package com.example.product_service.service;

import com.example.product_service.dto.InsuranceProductRequestDTO;
import com.example.product_service.dto.InsuranceProductResponseDTO;
import com.example.product_service.entity.InsuranceProductEntity;
import com.example.product_service.exception.BusinessException;
import com.example.product_service.mapper.InsuranceProductMapper;
import com.example.product_service.repository.InsuranceProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InsuranceProductServiceTest {

    @Mock
    private InsuranceProductRepository repository;

    @Mock
    private InsuranceProductMapper mapper;

    @InjectMocks
    private InsuranceProductService service;

    private InsuranceProductRequestDTO requestDTO;
    private InsuranceProductResponseDTO responseDTO;
    private InsuranceProductEntity entity;
    private final Long productID = 1L;

    @BeforeEach
    void setUp() {
        // Her testten önce kullanılacak ortak nesneleri hazırlıyoruz
        requestDTO = new InsuranceProductRequestDTO();
        requestDTO.setName("Sağlık Sigortası");
        requestDTO.setPrice(BigDecimal.valueOf(1500));

        entity = new InsuranceProductEntity();
        entity.setProductId(productID); // Eğer getProductId() metodu 'id' alanını okuyorsa
        entity.setName("Sağlık Sigortası");
        entity.setBasePrice(BigDecimal.valueOf(1500));
        entity.setIsDeleted(false);

        responseDTO = new InsuranceProductResponseDTO();
        responseDTO.setName("Sağlık Sigortası");
        responseDTO.setPrice(BigDecimal.valueOf(1500));
    }

    // ==========================================
    // saveProduct METODU TESTLERİ
    // ==========================================
    @Test
    void testSaveProduct_Success() {
        when(mapper.toEntity(requestDTO)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponseDto(entity)).thenReturn(responseDTO);

        InsuranceProductResponseDTO result = service.saveProduct(requestDTO);

        assertNotNull(result);
        assertEquals("Sağlık Sigortası", result.getName());

        verify(mapper).toEntity(requestDTO);
        verify(repository).save(entity);
        verify(mapper).toResponseDto(entity);
    }

    // ==========================================
    // getProductById METODU TESTLERİ
    // ==========================================
    @Test
    void testGetProductById_Success() {
        when(repository.findById(productID)).thenReturn(Optional.of(entity));
        when(mapper.toResponseDto(entity)).thenReturn(responseDTO);

        InsuranceProductResponseDTO result = service.getProductById(productID);

        assertNotNull(result);
        assertEquals("Sağlık Sigortası", result.getName());
        verify(repository).findById(productID);
    }

    @Test
    void testGetProductById_ThrowsException_WhenNotFound() {
        when(repository.findById(productID)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.getProductById(productID));

        assertTrue(exception.getMessage().contains("Ürün bulunamadı"));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(mapper, never()).toResponseDto(any()); // Hata fırladığı için mapper hiç çağrılmamalı
    }

    // ==========================================
    // updateProduct METODU TESTLERİ
    // ==========================================
    @Test
    void testUpdateProduct_Success() {
        when(repository.findById(productID)).thenReturn(Optional.of(entity));
        when(repository.save(any(InsuranceProductEntity.class))).thenReturn(entity);
        when(mapper.toResponseDto(entity)).thenReturn(responseDTO);

        InsuranceProductResponseDTO result = service.updateProduct(productID, requestDTO);

        assertNotNull(result);
        assertEquals("Sağlık Sigortası", result.getName());

        verify(repository).findById(productID);
        verify(repository).save(entity);
    }

    @Test
    void testUpdateProduct_ThrowsException_WhenNotFound() {
        when(repository.findById(productID)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.updateProduct(productID, requestDTO));

        assertTrue(exception.getMessage().contains("Güncellenecek ürün bulunamadı"));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(repository, never()).save(any());
    }

    // ==========================================
    // deleteProduct METODU TESTLERİ
    // ==========================================
    @Test
    void testDeleteProduct_Success() {
        when(repository.findById(productID)).thenReturn(Optional.of(entity));
        when(repository.save(any(InsuranceProductEntity.class))).thenReturn(entity);

        service.deleteProduct(productID);


        assertTrue(entity.getIsDeleted());

        verify(repository).findById(productID);
        verify(repository).save(entity);
    }

    @Test
    void testDeleteProduct_ThrowsException_WhenNotFound() {
        when(repository.findById(productID)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.deleteProduct(productID));

        assertTrue(exception.getMessage().contains("Silinmek istenen ürün bulunamadı"));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(repository, never()).save(any());
    }
}
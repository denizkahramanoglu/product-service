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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InsuranceProductServiceTest {

    @Mock
    private InsuranceProductRepository repository;

    @Mock
    private InsuranceProductMapper mapper;

    @InjectMocks
    private InsuranceProductService service;

    private InsuranceProductRequestDTO requestDto;
    private InsuranceProductEntity entity;
    private InsuranceProductResponseDTO responseDto;

    @BeforeEach
    void setUp() {
        requestDto = new InsuranceProductRequestDTO();
        requestDto.setName("Hayat Sigortası");
        requestDto.setPrice(new BigDecimal("5000.00"));

        entity = new InsuranceProductEntity();
        entity.setProductId(1L);
        entity.setName("Hayat Sigortası");
        entity.setBasePrice(new BigDecimal("5000.00"));
        entity.setDeleted(false);

        responseDto = new InsuranceProductResponseDTO();
        responseDto.setProductId(1L);
    }

    @Test
    void saveProduct_Success() {
        // Given
        when(mapper.toEntity(requestDto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponseDto(entity)).thenReturn(responseDto);

        // When
        InsuranceProductResponseDTO result = service.saveProduct(requestDto);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getProductId());
        verify(repository, times(1)).save(entity);
    }

    @Test
    void getProductById_Success() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toResponseDto(entity)).thenReturn(responseDto);

        // When
        InsuranceProductResponseDTO result = service.getProductById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getProductId());
    }

    @Test
    void getProductById_NotFound_ThrowsBusinessException() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.getProductById(1L);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertTrue(exception.getMessage().contains("Ürün bulunamadı"));
    }

    // --- YENİ EKLENEN UPDATE VE DELETE TESTLERİ ---

    @Test
    void updateProduct_Success() {
        // Given
        requestDto.setName("Güncellenmiş Hayat Sigortası");

        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any(InsuranceProductEntity.class))).thenReturn(entity);
        when(mapper.toResponseDto(entity)).thenReturn(responseDto);

        // When
        InsuranceProductResponseDTO result = service.updateProduct(1L, requestDto);

        // Then
        assertNotNull(result);
        // Save ve Mapper metodlarının tetiklendiğini doğruluyoruz
        verify(repository, times(1)).save(entity);
        verify(mapper, times(1)).toResponseDto(entity);

        // requestDto'dan gelen değerin entity'ye aktarıldığını (manuel mapping varsa) teyit edebiliriz
        assertEquals("Güncellenmiş Hayat Sigortası", entity.getName());
    }

    @Test
    void updateProduct_NotFound_ThrowsBusinessException() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.updateProduct(1L, requestDto);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        // Ürün bulunamadığı için veritabanına kaydetme (save) işlemi HİÇ çağrılmamalıdır
        verify(repository, never()).save(any());
    }

    @Test
    void deleteProduct_Success() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        // When
        service.deleteProduct(1L);

        // Then
        // Soft delete bayrağının true yapıldığını ve veritabanına kaydedildiğini doğruluyoruz
        assertTrue(entity.isDeleted());
        verify(repository, times(1)).save(entity);
    }

    @Test
    void deleteProduct_NotFound_ThrowsBusinessException() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.deleteProduct(1L);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        // Ürün bulunamadığı için veritabanına silme(güncelleme) işlemi HİÇ çağrılmamalıdır
        verify(repository, never()).save(any());
    }
}
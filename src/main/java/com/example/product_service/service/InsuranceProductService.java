package com.example.product_service.service;

import com.example.product_service.dto.InsuranceProductRequestDTO;
import com.example.product_service.dto.InsuranceProductResponseDTO;
import com.example.product_service.entity.InsuranceProductEntity;
import com.example.product_service.exception.BusinessException;
import com.example.product_service.mapper.InsuranceProductMapper;
import com.example.product_service.repository.InsuranceProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Sigorta ürünlerinin oluşturulması, okunması, güncellenmesi ve silinmesi (soft delete)
 * işlemlerinden sorumlu servis sınıfı.
 * Veritabanı etkileşimlerini ve Entity-DTO dönüşümlerini yönetir.
 *
 * @author deniz
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InsuranceProductService {

    private final InsuranceProductRepository repository;
    private final InsuranceProductMapper mapper;

    /**
     * Sisteme yeni bir sigorta ürünü kaydeder.
     *
     * @param requestDto Oluşturulacak sigorta ürününün bilgilerini içeren {@link InsuranceProductRequestDTO} nesnesi
     * @return Veritabanına kaydedilmiş ürünün bilgilerini içeren {@link InsuranceProductResponseDTO} nesnesi
     */
    @Transactional
    public InsuranceProductResponseDTO saveProduct(InsuranceProductRequestDTO requestDto) {

        InsuranceProductEntity entity = mapper.toEntity(requestDto);
        InsuranceProductEntity savedEntity = repository.save(entity);

        log.info("Yeni sigorta ürünü başarıyla kaydedildi. Ürün ID: {}", savedEntity.getProductId());

        return mapper.toResponseDto(savedEntity);
    }

    /**
     * Verilen ID'ye sahip sigorta ürününü veritabanından getirir.
     *
     * @param id Aranacak sigorta ürününün benzersiz ID'si
     * @return Bulunan ürünün detaylarını içeren {@link InsuranceProductResponseDTO} nesnesi
     * @throws BusinessException Eğer verilen ID ile eşleşen bir ürün bulunamazsa fırlatılır
     */
    public InsuranceProductResponseDTO getProductById(Long id) {

        InsuranceProductEntity entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Ürün bulunamadı! Geçersiz Ürün ID: " + id, HttpStatus.NOT_FOUND));

        return mapper.toResponseDto(entity);
    }

    /**
     * Verilen ID'ye sahip mevcut bir sigorta ürününün (isim ve taban fiyat) bilgilerini günceller.
     *
     * @param id Güncellenecek sigorta ürününün benzersiz ID'si
     * @param requestDto Ürüne ait güncel bilgileri içeren {@link InsuranceProductRequestDTO} nesnesi
     * @return Güncellenmiş ürünün detaylarını içeren {@link InsuranceProductResponseDTO} nesnesi
     * @throws BusinessException Eğer verilen ID ile eşleşen bir ürün bulunamazsa fırlatılır
     */
    @Transactional
    public InsuranceProductResponseDTO updateProduct(Long id, InsuranceProductRequestDTO requestDto) {

        InsuranceProductEntity existingProduct = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Güncellenecek ürün bulunamadı! ID: " + id, HttpStatus.NOT_FOUND));

        existingProduct.setName(requestDto.getName());
        existingProduct.setBasePrice(requestDto.getPrice());

        InsuranceProductEntity updatedProduct = repository.save(existingProduct);

        return mapper.toResponseDto(updatedProduct);
    }

    /**
     * Verilen ID'ye sahip sigorta ürününü sistemden siler.
     * Fiziksel silme yerine mantıksal silme (soft delete) işlemi uygulayarak
     * ürünün 'isDeleted' değerini 'true' yapar.
     *
     * @param id Silinecek sigorta ürününün benzersiz ID'si
     * @throws BusinessException Eğer verilen ID ile eşleşen bir ürün bulunamazsa fırlatılır
     */
    @Transactional
    public void deleteProduct(Long id) {
        InsuranceProductEntity product = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Silinmek istenen ürün bulunamadı! ID: " + id, HttpStatus.NOT_FOUND));

        product.setIsDeleted(true);
        repository.save(product);
    }
}
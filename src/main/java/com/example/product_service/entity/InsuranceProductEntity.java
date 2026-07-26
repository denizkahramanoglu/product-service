package com.example.product_service.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;


import java.math.BigDecimal;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SQLDelete(sql = "UPDATE insurance_product SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
@Table(name = "insurance_product")
public class InsuranceProductEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(name = "product_name", nullable = false)
    private String name;

    @Column(name = "base_price",nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "code", length = 3, nullable = false)
    private String code;

}
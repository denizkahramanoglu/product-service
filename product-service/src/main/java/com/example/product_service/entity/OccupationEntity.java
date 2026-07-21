package com.example.product_service.entity;
import com.example.product_service.enums.OccupationRisk;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "occupations")

public class OccupationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String title; // Örn: "Yazılım Mühendisi", "Madenci"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OccupationRisk riskLevel; // LOW, MEDIUM, HIGH, EXTREME

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;
}
package com.example.product_service.repository;

import com.example.product_service.entity.PersonalDiseasesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonalDiseaseRepository extends JpaRepository<PersonalDiseasesEntity, Long> {
}
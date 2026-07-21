package com.example.product_service.repository;

import com.example.product_service.entity.OccupationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OccupationRepository extends JpaRepository<OccupationEntity, Long> {

}
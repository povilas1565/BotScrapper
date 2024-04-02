package com.example.scrapper.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.scrapper.domain.models.CrossPromotion;

import java.util.List;

@Repository
public interface CrossPromotionRepository extends JpaRepository<CrossPromotion, Long> {
    List<CrossPromotion> findAllByEndDateIsNullOrderByStartDate();
}
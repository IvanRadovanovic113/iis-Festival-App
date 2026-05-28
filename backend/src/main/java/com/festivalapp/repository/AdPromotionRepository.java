package com.festivalapp.repository;

import com.festivalapp.model.AdPromotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AdPromotionRepository extends JpaRepository<AdPromotion, Long> {
    Optional<AdPromotion> findByAd_AdId(Long adId);
    List<AdPromotion> findAllByEndDateAndReminderSentAtIsNull(LocalDate endDate);
    void deleteByAd_AdId(Long adId);
}

package com.festivalapp.repository;

import com.festivalapp.model.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {

    List<PromoCode> findByFestival_FestivalIdOrderByValidFromDesc(Long festivalId);

    boolean existsByCodeAndFestival_FestivalId(String code, Long festivalId);

    boolean existsByCodeAndFestival_FestivalIdAndPromoCodeIdNot(String code, Long festivalId, Long promoCodeId);

    Optional<PromoCode> findByCodeAndFestival_FestivalId(String code, Long festivalId);
}

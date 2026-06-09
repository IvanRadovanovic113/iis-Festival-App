package com.festivalapp.prodaja.repository;

import com.festivalapp.prodaja.model.OcekivanaProdaja;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OcekivanaProdajaRepository extends JpaRepository<OcekivanaProdaja, Long> {

    Optional<OcekivanaProdaja> findByPricingPeriod_PricingPeriodId(Long pricingPeriodId);
}

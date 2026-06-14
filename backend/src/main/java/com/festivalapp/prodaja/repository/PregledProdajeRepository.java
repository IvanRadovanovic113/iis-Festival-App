package com.festivalapp.prodaja.repository;

import com.festivalapp.prodaja.model.PregledProdaje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PregledProdajeRepository extends JpaRepository<PregledProdaje, Long> {

    Optional<PregledProdaje> findTopByPricingPeriod_PricingPeriodIdOrderByDatumDesc(Long pricingPeriodId);
}

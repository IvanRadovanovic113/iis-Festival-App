package com.festivalapp.prodaja.repository;

import com.festivalapp.prodaja.model.KupacTier;
import com.festivalapp.prodaja.model.TierConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TierConfigRepository extends JpaRepository<TierConfig, KupacTier> {
}

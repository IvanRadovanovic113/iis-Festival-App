package com.festivalapp.repository;

import com.festivalapp.model.AdPhase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdPhaseRepository extends JpaRepository<AdPhase, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<AdPhase> findAllByOrderByOrderIndexAscNameAsc();

    Optional<AdPhase> findByNameIgnoreCase(String name);
}

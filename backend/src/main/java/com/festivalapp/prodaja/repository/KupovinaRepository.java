package com.festivalapp.prodaja.repository;

import com.festivalapp.prodaja.model.Kupovina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KupovinaRepository extends JpaRepository<Kupovina, Long> {
    List<Kupovina> findByKupac_KupacIdOrderByDatumDesc(Long kupacId);
}

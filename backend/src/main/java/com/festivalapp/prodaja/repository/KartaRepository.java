package com.festivalapp.prodaja.repository;

import com.festivalapp.prodaja.model.Karta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KartaRepository extends JpaRepository<Karta, Long> {
    List<Karta> findByKupovina_KupovinaId(Long kupovinaId);
}

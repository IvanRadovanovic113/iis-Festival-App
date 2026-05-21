package com.festivalapp.repository;

import com.festivalapp.model.Bina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BinaRepository extends JpaRepository<Bina, Long> {
    List<Bina> findByFestival_FestivalId(Long festivalId);
    boolean existsByNazivAndFestival_FestivalId(String naziv, Long festivalId);
    boolean existsByNazivAndFestival_FestivalIdAndBinaIdNot(String naziv, Long festivalId, Long binaId);
}

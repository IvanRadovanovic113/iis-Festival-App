package com.festivalapp.prodaja.repository;

import com.festivalapp.prodaja.model.Kupac;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KupacRepository extends JpaRepository<Kupac, Long> {

    Optional<Kupac> findByUser_Id(Long userId);
}

package com.festivalapp.prodaja.repository;

import com.festivalapp.prodaja.model.PriceLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface PriceLockRepository extends JpaRepository<PriceLock, Long> {

    @Modifying
    @Query("DELETE FROM PriceLock p WHERE p.expiresAt < :now")
    void deleteExpiredLocks(@Param("now") LocalDateTime now);
}

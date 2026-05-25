package com.festivalapp.prodaja.repository;

import com.festivalapp.prodaja.model.BundleDeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BundleDealRepository extends JpaRepository<BundleDeal, Long> {

    List<BundleDeal> findByTicketType_Festival_FestivalIdOrderByVaziOdDesc(Long festivalId);

    List<BundleDeal> findByTicketType_TicketTypeId(Long ticketTypeId);

    @Query("""
        SELECT b FROM BundleDeal b
        WHERE b.ticketType.ticketTypeId = :ticketTypeId
          AND b.active = true
          AND b.vaziOd <= :today
          AND b.vaziDo >= :today
          AND b.usedCount < b.dostupnoAkcija
        """)
    List<BundleDeal> findActiveByTicketType(
            @Param("ticketTypeId") Long ticketTypeId,
            @Param("today") LocalDate today);
}

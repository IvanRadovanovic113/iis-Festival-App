package com.festivalapp.prodaja.repository;

import com.festivalapp.prodaja.model.PricingPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PricingPeriodRepository extends JpaRepository<PricingPeriod, Long> {

    List<PricingPeriod> findByTicketType_TicketTypeIdOrderByStartDateAsc(Long ticketTypeId);

    @Transactional
    void deleteByTicketType_TicketTypeId(Long ticketTypeId);

    @Query("""
        SELECT COUNT(p) > 0 FROM PricingPeriod p
        WHERE p.ticketType.ticketTypeId = :ticketTypeId
          AND p.startDate <= :endDate
          AND p.endDate >= :startDate
        """)
    boolean existsOverlapping(
            @Param("ticketTypeId") Long ticketTypeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT COUNT(p) > 0 FROM PricingPeriod p
        WHERE p.ticketType.ticketTypeId = :ticketTypeId
          AND p.startDate <= :endDate
          AND p.endDate >= :startDate
          AND p.pricingPeriodId <> :excludeId
        """)
    boolean existsOverlappingExcluding(
            @Param("ticketTypeId") Long ticketTypeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeId") Long excludeId);

    @Query("""
        SELECT p FROM PricingPeriod p
        WHERE p.ticketType.ticketTypeId = :ticketTypeId
          AND p.startDate <= :today
          AND p.endDate >= :today
        """)
    Optional<PricingPeriod> findActiveForTicketType(
            @Param("ticketTypeId") Long ticketTypeId,
            @Param("today") LocalDate today);
}

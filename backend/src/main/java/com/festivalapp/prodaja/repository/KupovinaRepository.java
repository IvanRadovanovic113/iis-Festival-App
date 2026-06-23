package com.festivalapp.prodaja.repository;

import com.festivalapp.prodaja.model.Kupovina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface KupovinaRepository extends JpaRepository<Kupovina, Long> {
    List<Kupovina> findByKupac_KupacIdOrderByDatumDesc(Long kupacId);

    @Query("""
        SELECT COALESCE(SUM(k.kolicina), 0) FROM Kupovina k
        WHERE k.ticketType.ticketTypeId = :ticketTypeId
          AND k.datum >= :from
        """)
    Integer sumKolicinaSince(@Param("ticketTypeId") Long ticketTypeId,
                             @Param("from") LocalDateTime from);

    @Query("SELECT COALESCE(SUM(k.ukupnaCena), 0) FROM Kupovina k WHERE k.ticketType.festival.festivalId = :festivalId")
    BigDecimal sumRevenueForFestival(@Param("festivalId") Long festivalId);

    @Query(value = """
        SELECT TO_CHAR(k.datum, 'YYYY-MM-DD') AS day,
               SUM(k.ukupna_cena)             AS revenue,
               SUM(k.kolicina)                AS tickets
        FROM kupovine k
        JOIN ticket_types tt ON k.ticket_type_id = tt.ticket_type_id
        WHERE tt.festival_id = :festivalId
        GROUP BY TO_CHAR(k.datum, 'YYYY-MM-DD')
        ORDER BY TO_CHAR(k.datum, 'YYYY-MM-DD') ASC
        """, nativeQuery = true)
    List<Object[]> revenueByDayForFestival(@Param("festivalId") Long festivalId);

    @Query(value = """
        SELECT k.ticket_type_id,
               SUM(k.ukupna_cena) AS revenue,
               SUM(k.kolicina)    AS tickets
        FROM kupovine k
        JOIN ticket_types tt ON k.ticket_type_id = tt.ticket_type_id
        WHERE tt.festival_id = :festivalId
        GROUP BY k.ticket_type_id
        """, nativeQuery = true)
    List<Object[]> revenueByTicketTypeForFestival(@Param("festivalId") Long festivalId);

    @Query(value = """
        SELECT TO_CHAR(k.datum, 'YYYY-MM-DD') AS day,
               SUM(k.kolicina)                AS tickets,
               SUM(k.ukupna_cena)             AS revenue
        FROM kupovine k
        WHERE k.ticket_type_id = :ticketTypeId
          AND k.datum >= CAST(:from AS timestamp)
          AND k.datum <= CAST(:to   AS timestamp)
        GROUP BY TO_CHAR(k.datum, 'YYYY-MM-DD')
        ORDER BY TO_CHAR(k.datum, 'YYYY-MM-DD') ASC
        """, nativeQuery = true)
    List<Object[]> salesOverTimeForTicketType(
            @Param("ticketTypeId") Long ticketTypeId,
            @Param("from") String from,
            @Param("to") String to);

    @Query(value = """
        SELECT COALESCE(SUM(k.kolicina), 0)    AS tickets,
               COALESCE(SUM(k.ukupna_cena), 0) AS revenue
        FROM kupovine k
        WHERE k.ticket_type_id = :ticketTypeId
          AND k.datum >= CAST(:from AS timestamp)
          AND k.datum <= CAST(:to   AS timestamp)
        """, nativeQuery = true)
    List<Object[]> salesInPeriod(
            @Param("ticketTypeId") Long ticketTypeId,
            @Param("from") String from,
            @Param("to") String to);

    @Query("""
        SELECT k.promoCode.promoCodeId, COALESCE(SUM(k.ukupnaCena), 0)
        FROM Kupovina k
        WHERE k.ticketType.festival.festivalId = :festivalId
          AND k.promoCode IS NOT NULL
        GROUP BY k.promoCode.promoCodeId
        """)
    List<Object[]> revenueByPromoCodeForFestival(@Param("festivalId") Long festivalId);

    @Query("""
        SELECT k.bundleDeal.akcijaId, COALESCE(SUM(k.ukupnaCena), 0)
        FROM Kupovina k
        WHERE k.ticketType.festival.festivalId = :festivalId
          AND k.bundleDeal IS NOT NULL
        GROUP BY k.bundleDeal.akcijaId
        """)
    List<Object[]> revenueByBundleDealForFestival(@Param("festivalId") Long festivalId);

    @Query(value = """
        SELECT TO_CHAR(k.datum, 'YYYY-MM-DD') AS day,
               COUNT(k.kupovina_id)           AS purchases,
               SUM(k.ukupna_cena)             AS revenue
        FROM kupovine k
        JOIN ticket_types tt ON k.ticket_type_id = tt.ticket_type_id
        WHERE tt.festival_id = :festivalId
          AND k.akcija_id IS NOT NULL
        GROUP BY TO_CHAR(k.datum, 'YYYY-MM-DD')
        ORDER BY TO_CHAR(k.datum, 'YYYY-MM-DD') ASC
        """, nativeQuery = true)
    List<Object[]> dailyBundleImpactForFestival(@Param("festivalId") Long festivalId);

    @Query(value = """
        SELECT
            tt.ticket_type_id,
            tt.name,
            tt.dynamic_pricing_active,
            SUM(k.kolicina)                                                                AS total_tickets_sold,
            SUM(k.kolicina * pp.base_price)                                                AS baseline_revenue,
            SUM(k.kolicina * (COALESCE(ci.nova_cena, pp.base_price) - pp.base_price))     AS revenue_premium
        FROM kupovine k
        JOIN ticket_types tt ON k.ticket_type_id = tt.ticket_type_id
        JOIN pricing_periods pp
            ON  pp.ticket_type_id = k.ticket_type_id
            AND DATE(k.datum) BETWEEN pp.start_date AND pp.end_date
        LEFT JOIN LATERAL (
            SELECT ci2.nova_cena
            FROM cenovna_istorija ci2
            WHERE ci2.ticket_type_id = k.ticket_type_id
              AND ci2.datum <= k.datum
            ORDER BY ci2.datum DESC
            LIMIT 1
        ) ci ON true
        WHERE tt.festival_id = :festivalId
        GROUP BY tt.ticket_type_id, tt.name, tt.dynamic_pricing_active
        ORDER BY tt.ticket_type_id
        """, nativeQuery = true)
    List<Object[]> dynamicPricingSummaryForFestival(@Param("festivalId") Long festivalId);

    @Query(value = """
        SELECT
            k.ticket_type_id,
            TO_CHAR(k.datum, 'YYYY-MM-DD')              AS day,
            SUM(k.kolicina)                             AS tickets_sold,
            MAX(COALESCE(ci.nova_cena, pp.base_price))  AS price_on_day
        FROM kupovine k
        JOIN ticket_types tt ON k.ticket_type_id = tt.ticket_type_id
        JOIN pricing_periods pp
            ON  pp.ticket_type_id = k.ticket_type_id
            AND DATE(k.datum) BETWEEN pp.start_date AND pp.end_date
        LEFT JOIN LATERAL (
            SELECT ci2.nova_cena
            FROM cenovna_istorija ci2
            WHERE ci2.ticket_type_id = k.ticket_type_id
              AND ci2.datum <= k.datum
            ORDER BY ci2.datum DESC
            LIMIT 1
        ) ci ON true
        WHERE tt.festival_id = :festivalId
        GROUP BY k.ticket_type_id, TO_CHAR(k.datum, 'YYYY-MM-DD')
        ORDER BY k.ticket_type_id, day ASC
        """, nativeQuery = true)
    List<Object[]> dailyPriceSalesForFestival(@Param("festivalId") Long festivalId);
}

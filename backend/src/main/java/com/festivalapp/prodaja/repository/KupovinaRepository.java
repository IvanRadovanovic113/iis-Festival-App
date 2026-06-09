package com.festivalapp.prodaja.repository;

import com.festivalapp.prodaja.model.Kupovina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}

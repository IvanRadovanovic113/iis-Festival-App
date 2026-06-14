package com.festivalapp.prodaja.repository;

import com.festivalapp.prodaja.model.CenovnaIstorija;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CenovnaIstorijaRepository extends JpaRepository<CenovnaIstorija, Long> {

    Page<CenovnaIstorija> findByTicketType_TicketTypeIdOrderByDatumDesc(Long ticketTypeId, Pageable pageable);

    List<CenovnaIstorija> findTop5ByTicketType_TicketTypeIdAndPricingPeriod_PricingPeriodIdAndJeRucnaPromenaFalseOrderByDatumDesc(
            Long ticketTypeId, Long pricingPeriodId);
}

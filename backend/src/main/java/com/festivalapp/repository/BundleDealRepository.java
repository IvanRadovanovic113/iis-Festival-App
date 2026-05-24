package com.festivalapp.repository;

import com.festivalapp.model.BundleDeal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BundleDealRepository extends JpaRepository<BundleDeal, Long> {

    List<BundleDeal> findByTicketType_Festival_FestivalIdOrderByVaziOdDesc(Long festivalId);

    List<BundleDeal> findByTicketType_TicketTypeId(Long ticketTypeId);
}

package com.festivalapp.prodaja.repository;

import com.festivalapp.prodaja.model.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {
    List<TicketType> findByFestival_FestivalId(Long festivalId);
    boolean existsByNameAndFestival_FestivalId(String name, Long festivalId);
    boolean existsByNameAndFestival_FestivalIdAndTicketTypeIdNot(String name, Long festivalId, Long ticketTypeId);
    List<TicketType> findByDynamicPricingActiveTrue();
}

package com.festivalapp.repository;

import com.festivalapp.model.TicketTypeSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TicketTypeSegmentRepository extends JpaRepository<TicketTypeSegment, Long> {
    List<TicketTypeSegment> findByTicketType_TicketTypeId(Long ticketTypeId);

    @Transactional
    void deleteByTicketType_TicketTypeId(Long ticketTypeId);
}

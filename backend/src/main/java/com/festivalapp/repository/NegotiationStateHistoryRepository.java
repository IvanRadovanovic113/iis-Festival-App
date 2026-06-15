package com.festivalapp.repository;

import com.festivalapp.model.NegotiationStateHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NegotiationStateHistoryRepository extends JpaRepository<NegotiationStateHistory, Long> {
    List<NegotiationStateHistory> findByNegotiation_IdOrderByEntryTimeDesc(Long negotiationId);
    
    // Pronalazi trenutni aktivni zapis u istoriji (gde exitTime nije postavljen)
    Optional<NegotiationStateHistory> findByNegotiation_IdAndExitTimeIsNull(Long negotiationId);
}
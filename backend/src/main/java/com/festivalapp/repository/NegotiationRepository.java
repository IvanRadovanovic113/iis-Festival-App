package com.festivalapp.repository;

import com.festivalapp.model.Negotiation;
import com.festivalapp.dto.PerformerStatsDto;
import com.festivalapp.dto.StatePerformanceDTO;
import com.festivalapp.model.PerformerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

@Repository
public interface NegotiationRepository extends JpaRepository<Negotiation, Long>, JpaSpecificationExecutor<Negotiation> {
    List<Negotiation> findByStartedBy_Id(Long managerId);
    @Query("SELECT new com.festivalapp.dto.PerformerStatsDto(" +
       "n.performer.stageName, " +
       "COUNT(n), " +
       "SUM(CASE WHEN n.status = 'COMPLETED' THEN 1L ELSE 0L END), " +
       "SUM(CASE WHEN n.status = 'FAILED' THEN 1L ELSE 0L END), " +
       "(SUM(CASE WHEN n.status = 'COMPLETED' THEN 1.0 ELSE 0.0 END) / COUNT(n)) * 100) " +
       "FROM Negotiation n " +
       "WHERE (:genre IS NULL OR n.performer.genre = :genre) " +
       "AND (:type IS NULL OR n.performer.performerType = :type) " +
       "AND (n.offer.createdAt BETWEEN :startDate AND :endDate) " +
       "GROUP BY n.performer.stageName " +
       "ORDER BY SUM(CASE WHEN n.status = 'COMPLETED' THEN 1L ELSE 0L END) DESC")
    List<PerformerStatsDto> getPerformerStats(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("genre") String genre,
        @Param("type") PerformerType type
    );

    @Query(value = """
        SELECT 
            s.name AS state_name, 
            t.name AS template_name, 
            AVG(EXTRACT(EPOCH FROM (h.exit_time - h.entry_time)) / 3600.0) AS avg_duration, 
            COUNT(h.id) AS count
        FROM negotiation_state_history h
        INNER JOIN workflow_states s ON h.state_id = s.id
        INNER JOIN workflow_templates t ON s.template_id = t.id
        WHERE h.exit_time IS NOT NULL 
        AND (:templateId IS NULL OR t.id = :templateId)
        GROUP BY s.id, s.name, t.name
        ORDER BY avg_duration DESC
        """, nativeQuery = true)
    List<Object[]> getBottleneckReport(@Param("templateId") Long templateId);

    @Query(value = "SELECT * FROM fn_negotiation_efficiency(:start, :end)", nativeQuery = true)
    List<Object[]> getNegotiationEfficiency(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query(value = "SELECT * FROM fn_negotiation_duration_trend(:start, :end, :interval)", nativeQuery = true)
    List<Object[]> getNegotiationDurationTrend(@Param("start") LocalDate start, @Param("end") LocalDate end, @Param("interval") String interval);

    @Query(value = "SELECT * FROM fn_offer_outcomes(:start, :end)", nativeQuery = true)
    List<Object[]> getOfferOutcomes(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query(value = "SELECT * FROM fn_offer_duration_trend(:start, :end, :interval)", nativeQuery = true)
    List<Object[]> getOfferDurationTrend(@Param("start") LocalDate start, @Param("end") LocalDate end, @Param("interval") String interval);

    @Query(value = """
        SELECT n.id, p.stage_name, log.overdue_hours, log.deadline
        FROM negotiation_overdue_log log
        JOIN negotiations n ON log.negotiation_id = n.id
        JOIN performers p ON n.performer_id = p.performer_id
        ORDER BY log.overdue_hours DESC
        """, nativeQuery = true)
    List<Object[]> getCriticalNegotiations();
}
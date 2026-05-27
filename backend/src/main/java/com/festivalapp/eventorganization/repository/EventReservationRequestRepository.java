package com.festivalapp.eventorganization.repository;

import com.festivalapp.eventorganization.model.EventReservationRequest;
import com.festivalapp.eventorganization.model.EventReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface EventReservationRequestRepository extends JpaRepository<EventReservationRequest, Long> {
    List<EventReservationRequest> findByFestival_FestivalIdOrderByPerformanceDateAscStartTimeAsc(Long festivalId);

    List<EventReservationRequest> findByFestival_FestivalIdAndStatusOrderByPerformanceDateAscStartTimeAsc(
        Long festivalId,
        EventReservationStatus status
    );

    List<EventReservationRequest> findByStage_StageIdAndPerformanceDateAndStatusOrderByStartTimeAsc(
        Long stageId,
        LocalDate performanceDate,
        EventReservationStatus status
    );

    @Query("""
        select count(request) > 0
        from EventReservationRequest request
        where request.stage.stageId = :stageId
          and request.performanceDate = :performanceDate
          and request.status = com.festivalapp.eventorganization.model.EventReservationStatus.APPROVED
          and request.id <> :excludedRequestId
          and request.startTime < :endTime
          and request.endTime > :startTime
    """)
    boolean existsOverlappingApprovedRequest(
        @Param("stageId") Long stageId,
        @Param("performanceDate") LocalDate performanceDate,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime,
        @Param("excludedRequestId") Long excludedRequestId
    );
}

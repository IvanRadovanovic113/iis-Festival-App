package com.festivalapp.dto.eventorganization;

import com.festivalapp.model.eventorganization.EventReservationRequest;
import com.festivalapp.model.eventorganization.EventReservationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record EventReservationResponse(
    Long id,
    Long festivalId,
    String performerName,
    Long stageId,
    String stageName,
    LocalDate performanceDate,
    LocalTime startTime,
    LocalTime endTime,
    EventReservationStatus status,
    String notes,
    String reviewNote,
    LocalDateTime createdAt,
    LocalDateTime reviewedAt
) {
    public static EventReservationResponse from(EventReservationRequest request) {
        return new EventReservationResponse(
            request.getId(),
            request.getFestival().getFestivalId(),
            request.getPerformerName(),
            request.getStage().getStageId(),
            request.getStage().getName(),
            request.getPerformanceDate(),
            request.getStartTime(),
            request.getEndTime(),
            request.getStatus(),
            request.getNotes(),
            request.getReviewNote(),
            request.getCreatedAt(),
            request.getReviewedAt()
        );
    }
}

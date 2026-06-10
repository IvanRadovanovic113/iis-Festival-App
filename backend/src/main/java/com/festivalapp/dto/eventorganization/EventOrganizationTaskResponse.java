package com.festivalapp.dto.eventorganization;

import com.festivalapp.model.User;
import com.festivalapp.model.eventorganization.EventOrganizationTask;
import com.festivalapp.model.eventorganization.EventOrganizationTaskStatus;
import com.festivalapp.model.eventorganization.EventOrganizationTaskType;
import com.festivalapp.model.eventorganization.EventReservationRequest;
import com.festivalapp.model.eventorganization.RequestResource;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EventOrganizationTaskResponse(
    Long id,
    Long reservationRequestId,
    Long requestResourceId,
    Long resourceId,
    String title,
    String performerName,
    String stageName,
    LocalDate deadline,
    EventOrganizationTaskType type,
    EventOrganizationTaskStatus status,
    String resolutionNote,
    String rejectionReason,
    String actor,
    LocalDateTime changedAt
) {
    public static EventOrganizationTaskResponse from(EventOrganizationTask task) {
        RequestResource requestResource = task.getRequestResource();
        EventReservationRequest reservationRequest = requestResource.getReservationRequest();
        boolean procurement = task.getType() == EventOrganizationTaskType.PROCUREMENT;
        String resourceName = requestResource.getResource() == null
            ? requestResource.getRequestedName()
            : requestResource.getResource().getName();
        User actorUser = task.getStatus() == EventOrganizationTaskStatus.RESOLVED
            ? task.getResolvedBy()
            : task.getRejectedBy();
        LocalDateTime changedAt = task.getStatus() == EventOrganizationTaskStatus.RESOLVED
            ? task.getResolvedAt()
            : task.getRejectedAt();

        return new EventOrganizationTaskResponse(
            task.getId(),
            reservationRequest.getId(),
            requestResource.getId(),
            requestResource.getResource() == null ? null : requestResource.getResource().getId(),
            task.getTitle() == null ? (procurement ? "Procure: " : "Request: ") + resourceName : task.getTitle(),
            task.getPerformerName() == null ? reservationRequest.getPerformerName() : task.getPerformerName(),
            task.getStageName() == null ? reservationRequest.getStage().getName() : task.getStageName(),
            task.getDeadline() == null ? reservationRequest.getPerformanceDate().minusDays(1) : task.getDeadline(),
            task.getType(),
            task.getStatus(),
            task.getResolutionNote(),
            task.getRejectionReason(),
            actorUser == null ? null : actorUser.getUsername(),
            changedAt
        );
    }
}

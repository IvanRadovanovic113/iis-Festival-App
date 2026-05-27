package com.festivalapp.eventorganization.controller;

import com.festivalapp.eventorganization.dto.*;
import com.festivalapp.eventorganization.model.EventReservationStatus;
import com.festivalapp.eventorganization.service.EventOrganizationService;
import com.festivalapp.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/event-organization")
@RequiredArgsConstructor
public class EventOrganizationController {

    private final EventOrganizationService eventOrganizationService;

    @GetMapping("/requests")
    public ResponseEntity<List<EventReservationResponse>> getReservationRequests(
            @RequestParam(required = false) EventReservationStatus status,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(eventOrganizationService.getReservationRequests(status, user));
    }

    @PostMapping("/requests")
    public ResponseEntity<EventReservationResponse> createReservationRequest(
            @Valid @RequestBody EventReservationCreateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(eventOrganizationService.createReservationRequest(request, user));
    }

    @PutMapping("/requests/{requestId}/approve")
    public ResponseEntity<EventReservationResponse> approveReservationRequest(
            @PathVariable Long requestId,
            @RequestBody EventReservationReviewRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(eventOrganizationService.approveReservationRequest(requestId, request, user));
    }

    @PutMapping("/requests/{requestId}/reject")
    public ResponseEntity<EventReservationResponse> rejectReservationRequest(
            @PathVariable Long requestId,
            @RequestBody EventReservationReviewRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(eventOrganizationService.rejectReservationRequest(requestId, request, user));
    }

    @GetMapping("/resources")
    public ResponseEntity<List<EventResourceResponse>> getResources(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(eventOrganizationService.getResources(user));
    }

    @PostMapping("/resources")
    public ResponseEntity<EventResourceResponse> createResource(
            @Valid @RequestBody EventResourceRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(eventOrganizationService.createResource(request, user));
    }

    @PutMapping("/resources/{resourceId}")
    public ResponseEntity<EventResourceResponse> updateResource(
            @PathVariable Long resourceId,
            @Valid @RequestBody EventResourceRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(eventOrganizationService.updateResource(resourceId, request, user));
    }

    @DeleteMapping("/resources/{resourceId}")
    public ResponseEntity<Void> deleteResource(
            @PathVariable Long resourceId,
            @AuthenticationPrincipal User user) {
        eventOrganizationService.deleteResource(resourceId, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stages/{stageId}/resources")
    public ResponseEntity<List<StageResourceResponse>> getStageResources(
            @PathVariable Long stageId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(eventOrganizationService.getStageResources(stageId, user));
    }

    @PostMapping("/stages/{stageId}/resources")
    public ResponseEntity<StageResourceResponse> assignResourceToStage(
            @PathVariable Long stageId,
            @Valid @RequestBody StageResourceRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(eventOrganizationService.assignResourceToStage(stageId, request, user));
    }

    @PutMapping("/stages/{stageId}/resources/{resourceId}")
    public ResponseEntity<StageResourceResponse> updateStageResource(
            @PathVariable Long stageId,
            @PathVariable Long resourceId,
            @Valid @RequestBody StageResourceRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(eventOrganizationService.updateStageResource(stageId, resourceId, request, user));
    }

    @DeleteMapping("/stages/{stageId}/resources/{resourceId}")
    public ResponseEntity<Void> removeResourceFromStage(
            @PathVariable Long stageId,
            @PathVariable Long resourceId,
            @AuthenticationPrincipal User user) {
        eventOrganizationService.removeResourceFromStage(stageId, resourceId, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stages/{stageId}/timetable")
    public ResponseEntity<List<TimetableSlotResponse>> getStageTimetable(
            @PathVariable Long stageId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(eventOrganizationService.getStageTimetable(stageId, date, user));
    }
}

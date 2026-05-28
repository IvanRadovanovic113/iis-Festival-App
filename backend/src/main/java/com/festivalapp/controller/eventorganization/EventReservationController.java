package com.festivalapp.controller.eventorganization;

import com.festivalapp.dto.eventorganization.EventReservationResponse;
import com.festivalapp.dto.eventorganization.EventReservationReviewRequest;
import com.festivalapp.dto.eventorganization.EventReservationScheduleRequest;
import com.festivalapp.dto.eventorganization.TimetableSlotResponse;
import com.festivalapp.model.User;
import com.festivalapp.model.eventorganization.EventReservationStatus;
import com.festivalapp.service.eventorganization.EventReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/event-organization")
@RequiredArgsConstructor
public class EventReservationController {

    private final EventReservationService eventReservationService;

    @GetMapping("/requests")
    public ResponseEntity<List<EventReservationResponse>> getReservationRequests(
            @RequestParam(required = false) EventReservationStatus status,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(eventReservationService.getReservationRequests(status, user));
    }

    @PutMapping("/requests/{requestId}/approve")
    public ResponseEntity<EventReservationResponse> approveReservationRequest(
            @PathVariable Long requestId,
            @RequestBody EventReservationReviewRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(eventReservationService.approveReservationRequest(requestId, request, user));
    }

    @PutMapping("/requests/{requestId}/schedule")
    public ResponseEntity<EventReservationResponse> scheduleReservationRequest(
            @PathVariable Long requestId,
            @Valid @RequestBody EventReservationScheduleRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(eventReservationService.scheduleReservationRequest(requestId, request, user));
    }

    @PutMapping("/requests/{requestId}/reject")
    public ResponseEntity<EventReservationResponse> rejectReservationRequest(
            @PathVariable Long requestId,
            @RequestBody EventReservationReviewRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(eventReservationService.rejectReservationRequest(requestId, request, user));
    }

    @GetMapping("/stages/{stageId}/timetable")
    public ResponseEntity<List<TimetableSlotResponse>> getStageTimetable(
            @PathVariable Long stageId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(eventReservationService.getStageTimetable(stageId, date, user));
    }
}

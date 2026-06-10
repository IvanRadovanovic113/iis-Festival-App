package com.festivalapp.service.eventorganization;

import com.festivalapp.dto.eventorganization.EventReservationResponse;
import com.festivalapp.dto.eventorganization.EventReservationScheduleRequest;
import com.festivalapp.dto.eventorganization.TimetableSlotResponse;
import com.festivalapp.model.Festival;
import com.festivalapp.model.User;
import com.festivalapp.model.eventorganization.EventReservationRequest;
import com.festivalapp.model.eventorganization.EventReservationStatus;
import com.festivalapp.repository.eventorganization.EventReservationRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventReservationService {

    private static final String AVAILABLE_STATUS = "AVAILABLE";

    private final EventReservationRequestRepository reservationRequestRepository;
    private final EventOrganizationAccessService accessService;
    private final EventOrganizationTaskService taskService;

    public List<EventReservationResponse> getReservationRequests(EventReservationStatus status, User user) {
        Festival festival = accessService.requireEventOrganizerFestival(user);
        List<EventReservationRequest> requests;
        if (festival == null) {
            requests = status == null
                ? reservationRequestRepository.findAll()
                : reservationRequestRepository.findAll().stream()
                    .filter(request -> request.getStatus() == status)
                    .toList();
        } else {
            requests = status == null
                ? reservationRequestRepository.findByFestival_FestivalIdOrderByPerformanceDateAscStartTimeAsc(festival.getFestivalId())
                : reservationRequestRepository.findByFestival_FestivalIdAndStatusOrderByPerformanceDateAscStartTimeAsc(
                    festival.getFestivalId(),
                    status
                );
        }
        return requests.stream().map(EventReservationResponse::from).toList();
    }

    public EventReservationResponse approveReservationRequest(Long requestId, User user) {
        Festival festival = accessService.requireEventOrganizerFestival(user);
        EventReservationRequest reservationRequest = accessService.requireReservationRequest(requestId, festival);
        validateReservationTime(reservationRequest.getStartTime(), reservationRequest.getEndTime());
        validateStageAvailability(reservationRequest);
        reservationRequest.setStatus(EventReservationStatus.APPROVED);
        reservationRequest.setReviewedAt(LocalDateTime.now());
        EventReservationRequest savedRequest = reservationRequestRepository.save(reservationRequest);
        syncTasksWithoutBlockingReservation(festival, savedRequest.getId());
        return EventReservationResponse.from(savedRequest);
    }

    public EventReservationResponse scheduleReservationRequest(Long requestId, EventReservationScheduleRequest scheduleRequest, User user) {
        Festival festival = accessService.requireEventOrganizerFestival(user);
        EventReservationRequest reservationRequest = accessService.requireReservationRequest(requestId, festival);
        if (reservationRequest.getStatus() != EventReservationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending requests can be scheduled");
        }

        long durationMinutes = java.time.Duration.between(
            reservationRequest.getStartTime(),
            reservationRequest.getEndTime()
        ).toMinutes();
        if (durationMinutes <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation request duration is invalid");
        }

        LocalTime startTime = scheduleRequest.getStartTime();
        LocalTime endTime = startTime.plusMinutes(durationMinutes);
        validateReservationTime(startTime, endTime);

        reservationRequest.setStartTime(startTime);
        reservationRequest.setEndTime(endTime);
        validateStageAvailability(reservationRequest);

        reservationRequest.setStatus(EventReservationStatus.APPROVED);
        reservationRequest.setReviewedAt(LocalDateTime.now());
        EventReservationRequest savedRequest = reservationRequestRepository.save(reservationRequest);
        syncTasksWithoutBlockingReservation(festival, savedRequest.getId());
        return EventReservationResponse.from(savedRequest);
    }

    public EventReservationResponse rejectReservationRequest(Long requestId, User user) {
        Festival festival = accessService.requireEventOrganizerFestival(user);
        EventReservationRequest reservationRequest = accessService.requireReservationRequest(requestId, festival);
        reservationRequest.setStatus(EventReservationStatus.REJECTED);
        reservationRequest.setReviewedAt(LocalDateTime.now());
        return EventReservationResponse.from(reservationRequestRepository.save(reservationRequest));
    }

    public List<TimetableSlotResponse> getStageTimetable(Long stageId, LocalDate date, User user) {
        Festival festival = accessService.requireEventOrganizerFestival(user);
        accessService.requireStage(stageId, festival);
        List<EventReservationRequest> approvedRequests = reservationRequestRepository
            .findByStage_StageIdAndPerformanceDateAndStatusOrderByStartTimeAsc(
                stageId,
                date,
                EventReservationStatus.APPROVED
            );
        List<TimetableSlotResponse> slots = new ArrayList<>();
        for (int hour = 12; hour < 24; hour++) {
            LocalTime startTime = LocalTime.of(hour, 0);
            LocalTime endTime = hour == 23 ? LocalTime.MIDNIGHT : LocalTime.of(hour + 1, 0);
            LocalTime comparisonEndTime = hour == 23 ? LocalTime.MAX : endTime;
            EventReservationRequest occupyingRequest = approvedRequests.stream()
                .filter(request -> request.getStartTime().isBefore(comparisonEndTime) && request.getEndTime().isAfter(startTime))
                .findFirst()
                .orElse(null);
            slots.add(new TimetableSlotResponse(
                date,
                startTime,
                endTime,
                occupyingRequest == null ? AVAILABLE_STATUS : "OCCUPIED",
                occupyingRequest == null ? null : occupyingRequest.getPerformerName()
            ));
        }
        return slots;
    }

    private void validateReservationTime(LocalTime startTime, LocalTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start time must be before end time");
        }
    }

    private void validateStageAvailability(EventReservationRequest reservationRequest) {
        boolean overlaps = reservationRequestRepository.existsOverlappingApprovedRequest(
            reservationRequest.getStage().getStageId(),
            reservationRequest.getPerformanceDate(),
            reservationRequest.getStartTime(),
            reservationRequest.getEndTime(),
            reservationRequest.getId()
        );
        if (overlaps) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Selected stage already has an approved request in this time slot");
        }
    }

    private void syncTasksWithoutBlockingReservation(Festival festival, Long reservationRequestId) {
        try {
            taskService.syncOpenTasks(festival);
        } catch (RuntimeException ex) {
            log.warn("Reservation {} was approved, but task synchronization failed", reservationRequestId, ex);
        }
    }
}

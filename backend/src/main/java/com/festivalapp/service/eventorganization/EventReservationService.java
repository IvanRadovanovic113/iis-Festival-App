package com.festivalapp.service.eventorganization;

import com.festivalapp.dto.eventorganization.EventReservationResponse;
import com.festivalapp.dto.eventorganization.EventReservationScheduleRequest;
import com.festivalapp.dto.eventorganization.SlotSuggestionResponse;
import com.festivalapp.dto.eventorganization.TimetableSlotResponse;
import com.festivalapp.model.Festival;
import com.festivalapp.model.PerformerPopularity;
import com.festivalapp.model.User;
import com.festivalapp.model.eventorganization.EventReservationRequest;
import com.festivalapp.model.eventorganization.EventReservationStatus;
import com.festivalapp.model.eventorganization.RequestResourceStatus;
import com.festivalapp.repository.eventorganization.EventReservationRequestRepository;
import com.festivalapp.repository.eventorganization.RequestResourceRepository;
import lombok.RequiredArgsConstructor;
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
public class EventReservationService {

    private static final String AVAILABLE_STATUS = "AVAILABLE";

    private final EventReservationRequestRepository reservationRequestRepository;
    private final RequestResourceRepository requestResourceRepository;
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
        taskService.createTasksForReservation(savedRequest);
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
        taskService.createTasksForReservation(savedRequest);
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

    public SlotSuggestionResponse suggestSlot(Long requestId, User user) {
        Festival festival = accessService.requireEventOrganizerFestival(user);
        EventReservationRequest request = accessService.requireReservationRequest(requestId, festival);

        int durationMinutes = (int) java.time.Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
        if (durationMinutes <= 0) {
            return new SlotSuggestionResponse(null);
        }

        int targetMinutes = resolveTargetMinutes(request); // idealno vreme pocetka 

        List<EventReservationRequest> occupiedRequests = reservationRequestRepository.findOccupiedOnStageDate(
            request.getStage().getStageId(),
            request.getPerformanceDate(),
            List.of(EventReservationStatus.PENDING, EventReservationStatus.APPROVED),
            requestId
        );

        Integer bestStartMin = null;
        int bestScore = Integer.MAX_VALUE;

        for (int hour = 14; hour <= 23; hour++) {
            int startMin = hour * 60;
            int endMin = startMin + durationMinutes;

            if (endMin >= 24 * 60) continue;
            if (overlapsAny(startMin, endMin, occupiedRequests)) continue;

            int score = Math.abs(startMin - targetMinutes) + computeResourcePenalty(request, startMin, durationMinutes);
            if (score < bestScore) {
                bestScore = score;
                bestStartMin = startMin;
            }
        }

        if (bestStartMin == null) {
            return new SlotSuggestionResponse(null);
        }
        return new SlotSuggestionResponse(String.format("%02d:00", bestStartMin / 60));
    }

    private boolean overlapsAny(int startMin, int endMin, List<EventReservationRequest> requests) {
        for (EventReservationRequest occupied : requests) {
            int occupiedStart = occupied.getStartTime().getHour() * 60 + occupied.getStartTime().getMinute();
            int occupiedEnd = occupied.getEndTime().getHour() * 60 + occupied.getEndTime().getMinute();
            if (occupiedStart < endMin && occupiedEnd > startMin) {
                return true;
            }
        }
        return false;
    }

    private int resolveTargetMinutes(EventReservationRequest request) {
        PerformerPopularity popularity = null;
        try {
            if (request.getContract() != null) {
                popularity = request.getContract().getNegotiation().getPerformer().getPopularity();
            }
        } catch (Exception ignored) {}

        if (popularity == null) popularity = PerformerPopularity.POPULAR;

        return switch (popularity) {
            case HEADLINER -> 21 * 60;
            case POPULAR -> 19 * 60;
            case EMERGING -> 17 * 60;
        };
    }

    // Da li je resurs potpuno zauzet
    private int computeResourcePenalty(EventReservationRequest request, int candidateStartMin, int durationMinutes) {
        LocalTime candidateStart = LocalTime.of(candidateStartMin / 60, candidateStartMin % 60);
        LocalTime candidateEnd = LocalTime.of((candidateStartMin + durationMinutes) / 60, (candidateStartMin + durationMinutes) % 60);

        int penalty = 0;
        for (var rr : requestResourceRepository.findByReservationRequest_IdOrderByResource_NameAsc(request.getId())) {
            if (rr.getResource() == null) continue;
            Integer overlapping = requestResourceRepository.sumOverlappingQuantityByResource(
                rr.getResource().getId(),
                request.getId(),
                request.getPerformanceDate(),
                candidateStart,
                candidateEnd,
                RequestResourceStatus.CONFIRMED
            );
            if (overlapping != null && overlapping >= rr.getResource().getTotalQuantity()) {
                penalty += 30;
            }
        }
        return penalty;
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

}

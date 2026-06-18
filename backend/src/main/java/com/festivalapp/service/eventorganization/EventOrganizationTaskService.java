package com.festivalapp.service.eventorganization;

import com.festivalapp.dto.eventorganization.EventOrganizationTaskResponse;
import com.festivalapp.dto.eventorganization.RejectTaskRequest;
import com.festivalapp.dto.eventorganization.ResolveTaskRequest;
import com.festivalapp.model.Festival;
import com.festivalapp.model.User;
import com.festivalapp.model.eventorganization.EventOrganizationTask;
import com.festivalapp.model.eventorganization.EventOrganizationTaskStatus;
import com.festivalapp.model.eventorganization.EventOrganizationTaskType;
import com.festivalapp.model.eventorganization.EventReservationRequest;
import com.festivalapp.model.eventorganization.RequestResource;
import com.festivalapp.model.eventorganization.RequestResourceStatus;
import com.festivalapp.repository.eventorganization.EventOrganizationTaskRepository;
import com.festivalapp.repository.eventorganization.RequestResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventOrganizationTaskService {

    private final EventOrganizationTaskRepository taskRepository;
    private final RequestResourceRepository requestResourceRepository;
    private final EventOrganizationAccessService accessService;

    @Transactional
    public List<EventOrganizationTaskResponse> getTasks(EventOrganizationTaskStatus status, User user) {
        Festival festival = accessService.requireEventOrganizerFestival(user);

        List<EventOrganizationTask> tasks;
        if (festival == null) {
            tasks = status == null
                ? taskRepository.findAllOrdered()
                : taskRepository.findByStatusOrdered(status);
        } else {
            tasks = status == null
                ? taskRepository.findByFestival(festival)
                : taskRepository.findByFestivalAndStatus(
                    festival,
                    status
                );
        }

        return tasks.stream().map(EventOrganizationTaskResponse::from).toList();
    }

    @Transactional
    public EventOrganizationTaskResponse resolveTask(Long taskId, ResolveTaskRequest request, User user) {
        Festival festival = accessService.requireEventOrganizerFestival(user);
        EventOrganizationTask task = requireTask(taskId, festival);
        if (task.getStatus() != EventOrganizationTaskStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only open tasks can be resolved");
        }
        task.setStatus(EventOrganizationTaskStatus.RESOLVED);
        task.setResolutionNote(blankToNull(request.note()));
        task.setResolvedBy(user);
        task.setResolvedAt(LocalDateTime.now());
        task.setRejectionReason(null);
        task.setRejectedBy(null);
        task.setRejectedAt(null);
        return EventOrganizationTaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public EventOrganizationTaskResponse rejectTask(Long taskId, RejectTaskRequest request, User user) {
        Festival festival = accessService.requireEventOrganizerFestival(user);
        EventOrganizationTask task = requireTask(taskId, festival);
        if (task.getStatus() != EventOrganizationTaskStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only open tasks can be rejected");
        }
        task.setStatus(EventOrganizationTaskStatus.REJECTED);
        task.setRejectionReason(request.reason().trim());
        task.setRejectedBy(user);
        task.setRejectedAt(LocalDateTime.now());
        task.setResolutionNote(null);
        task.setResolvedBy(null);
        task.setResolvedAt(null);
        return EventOrganizationTaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public void createTasksForReservation(EventReservationRequest reservation) {
        List<RequestResource> resources = requestResourceRepository.findByReservationRequest_IdOrderByResource_NameAsc(reservation.getId());
        for (RequestResource requestResource : resources) {
            if (!needsTask(requestResource)) {
                requestResource.setStatus(RequestResourceStatus.CONFIRMED);
                requestResourceRepository.save(requestResource);
                continue;
            }
            if (taskRepository.existsByRequestResource_Id(requestResource.getId())) continue;
            taskRepository.save(EventOrganizationTask.builder()
                .requestResource(requestResource)
                .type(resolveTaskType(requestResource))
                .status(EventOrganizationTaskStatus.OPEN)
                .title(taskTitle(requestResource))
                .performerName(reservation.getPerformerName())
                .stageName(reservation.getStage().getName())
                .deadline(taskDeadline(requestResource))
                .build());
        }
    }

    private EventOrganizationTask requireTask(Long taskId, Festival festival) {
        EventOrganizationTask task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task was not found"));
        if (festival != null && !task.getRequestResource().getReservationRequest().getFestival().getFestivalId().equals(festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Task does not belong to your festival");
        }
        return task;
    }

    // HELPERI

    private boolean needsTask(RequestResource requestResource) {
        if (requestResource.getResource() == null) {
            return true; // custom resurs, uvek kreira task
        }
        EventReservationRequest reservation = requestResource.getReservationRequest();
        Integer confirmed = requestResourceRepository.sumOverlappingQuantityByResource(
            requestResource.getResource().getId(),
            reservation.getId(),
            reservation.getPerformanceDate(),
            reservation.getStartTime(),
            reservation.getEndTime(),
            RequestResourceStatus.CONFIRMED
        );
        int available = requestResource.getResource().getTotalQuantity() - (confirmed != null ? confirmed : 0);
        return available < requestResource.getQuantity();
    }

    private EventOrganizationTaskType resolveTaskType(RequestResource requestResource) {
        return requestResource.getResource() == null
            ? EventOrganizationTaskType.NON_EXISTING
            : EventOrganizationTaskType.PROCUREMENT;
    }

    // Odredjivanje naziva taska
    private String taskTitle(RequestResource requestResource) {
        EventOrganizationTaskType type = resolveTaskType(requestResource);
        String resourceName = requestResource.getResource() == null
            ? requestResource.getRequestedName()
            : requestResource.getResource().getName();
        return (type == EventOrganizationTaskType.PROCUREMENT ? "Procure: " : "Request: ") + resourceName;
    }

    // Odredjivanje roka za resavanje/odbijanje taska
    private LocalDate taskDeadline(RequestResource requestResource) {
        return requestResource.getReservationRequest().getPerformanceDate().minusDays(1);
    }

    private String blankToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}

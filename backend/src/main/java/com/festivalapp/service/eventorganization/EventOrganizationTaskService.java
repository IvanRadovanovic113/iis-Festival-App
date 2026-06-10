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
import com.festivalapp.model.eventorganization.EventReservationStatus;
import com.festivalapp.model.eventorganization.EventResource;
import com.festivalapp.model.eventorganization.RequestResource;
import com.festivalapp.model.eventorganization.RequestResourceStatus;
import com.festivalapp.model.eventorganization.StageResource;
import com.festivalapp.repository.eventorganization.EventOrganizationTaskRepository;
import com.festivalapp.repository.eventorganization.RequestResourceRepository;
import com.festivalapp.repository.eventorganization.StageResourceRepository;
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
    private final StageResourceRepository stageResourceRepository;
    private final EventOrganizationAccessService accessService;

    @Transactional
    public List<EventOrganizationTaskResponse> getTasks(EventOrganizationTaskStatus status, User user) {
        Festival festival = accessService.requireEventOrganizerFestival(user);
        syncOpenTasks(festival);

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
    public void syncOpenTasks(Festival festival) {
        List<RequestResource> requestResources = festival == null
            ? requestResourceRepository.findAll()
            : requestResourceRepository.findByReservationRequest_Festival(festival);

        requestResources.stream()
            .filter(requestResource -> requestResource.getReservationRequest().getStatus() == EventReservationStatus.APPROVED)
            .filter(this::needsTask)
            .filter(requestResource -> !taskRepository.existsByRequestResource_Id(requestResource.getId()))
            .map(requestResource -> EventOrganizationTask.builder()
                .requestResource(requestResource)
                .type(resolveTaskType(requestResource))
                .status(EventOrganizationTaskStatus.OPEN)
                .title(taskTitle(requestResource))
                .performerName(requestResource.getReservationRequest().getPerformerName())
                .stageName(requestResource.getReservationRequest().getStage().getName())
                .deadline(taskDeadline(requestResource))
                .build())
            .forEach(taskRepository::save);
    }

    private EventOrganizationTask requireTask(Long taskId, Festival festival) {
        EventOrganizationTask task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task was not found"));
        if (festival != null && !task.getRequestResource().getReservationRequest().getFestival().getFestivalId().equals(festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Task does not belong to your festival");
        }
        return task;
    }

    private boolean needsTask(RequestResource requestResource) {
        if (requestResource.getStatus() == RequestResourceStatus.CONFIRMED) {
            return false;
        }
        if (requestResource.getResource() == null) {
            return true;
        }
        if (requestResource.getStatus() == RequestResourceStatus.UNAVAILABLE) {
            return true;
        }
        return !canFulfill(requestResource);
    }

    private boolean canFulfill(RequestResource requestResource) {
        EventReservationRequest reservationRequest = requestResource.getReservationRequest();
        EventResource resource = requestResource.getResource();
        if (resource == null) {
            return false;
        }
        Integer overlappingQuantity = requestResourceRepository.sumOverlappingQuantityByResource(
            resource.getId(),
            reservationRequest.getId(),
            reservationRequest.getPerformanceDate(),
            reservationRequest.getStartTime(),
            reservationRequest.getEndTime(),
            RequestResourceStatus.CONFIRMED
        );
        int availableQuantity = resource.getTotalQuantity() - overlappingQuantity;
        if (availableQuantity < requestResource.getQuantity()) {
            return false;
        }

        boolean assignedToReservationStage = stageResourceRepository
            .findByStage_StageIdAndResource_Id(reservationRequest.getStage().getStageId(), resource.getId())
            .filter(stageResource -> stageResource.getQuantity() >= requestResource.getQuantity())
            .isPresent();
        if (assignedToReservationStage) {
            return true;
        }
        if (!Boolean.TRUE.equals(resource.getShareable())) {
            return false;
        }

        return stageResourceRepository.findByResource_Id(resource.getId()).stream()
            .filter(stageResource -> !stageResource.getStage().getStageId().equals(reservationRequest.getStage().getStageId()))
            .anyMatch(stageResource -> stageResource.getQuantity() >= requestResource.getQuantity());
    }

    private EventOrganizationTaskType resolveTaskType(RequestResource requestResource) {
        return requestResource.getResource() == null
            ? EventOrganizationTaskType.NON_EXISTING
            : EventOrganizationTaskType.PROCUREMENT;
    }

    private String taskTitle(RequestResource requestResource) {
        EventOrganizationTaskType type = resolveTaskType(requestResource);
        String resourceName = requestResource.getResource() == null
            ? requestResource.getRequestedName()
            : requestResource.getResource().getName();
        return (type == EventOrganizationTaskType.PROCUREMENT ? "Procure: " : "Request: ") + resourceName;
    }

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

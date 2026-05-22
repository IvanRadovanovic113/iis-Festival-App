package com.festivalapp.eventorganization.service;

import com.festivalapp.eventorganization.dto.*;
import com.festivalapp.eventorganization.model.EventResource;
import com.festivalapp.eventorganization.model.StageResource;
import com.festivalapp.eventorganization.repository.EventResourceRepository;
import com.festivalapp.eventorganization.repository.StageResourceRepository;
import com.festivalapp.model.*;
import com.festivalapp.repository.StageRepository;
import com.festivalapp.repository.UserFestivalAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventOrganizationService {

    private static final String AVAILABLE_STATUS = "AVAILABLE";

    private final EventResourceRepository eventResourceRepository;
    private final StageResourceRepository stageResourceRepository;
    private final StageRepository stageRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;

    private Festival requireEventOrganizerFestival(User user) {
        if (user.getRole() == Role.ADMIN) {
            return null;
        }
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Festival assignment is required"));
        if (assignment.getRole() != Role.EVENT_ORGANIZER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Event organizer role is required");
        }
        return assignment.getFestival();
    }

    private Stage requireStage(Long stageId, Festival festival) {
        Stage stage = stageRepository.findById(stageId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stage was not found"));
        if (festival != null && !stage.getFestival().getFestivalId().equals(festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Stage does not belong to your festival");
        }
        return stage;
    }

    private EventResource requireResource(Long resourceId, Festival festival) {
        EventResource resource = eventResourceRepository.findById(resourceId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource was not found"));
        if (festival != null && !resource.getFestival().getFestivalId().equals(festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Resource does not belong to your festival");
        }
        return resource;
    }

    public List<EventResourceResponse> getResources(User user) {
        Festival festival = requireEventOrganizerFestival(user);
        List<EventResource> resources = festival == null
            ? eventResourceRepository.findAll()
            : eventResourceRepository.findByFestival_FestivalIdOrderByNameAsc(festival.getFestivalId());
        return resources.stream().map(EventResourceResponse::from).toList();
    }

    public EventResourceResponse createResource(EventResourceRequest request, User user) {
        Festival festival = requireEventOrganizerFestival(user);
        if (festival == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin users must assign resources through an event organizer");
        }
        if (eventResourceRepository.existsByNameIgnoreCaseAndFestival_FestivalId(request.getName(), festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A resource with this name already exists");
        }
        EventResource resource = EventResource.builder()
            .name(request.getName())
            .type(request.getType())
            .description(request.getDescription())
            .totalQuantity(request.getTotalQuantity())
            .festival(festival)
            .build();
        return EventResourceResponse.from(eventResourceRepository.save(resource));
    }

    public EventResourceResponse updateResource(Long resourceId, EventResourceRequest request, User user) {
        Festival festival = requireEventOrganizerFestival(user);
        EventResource resource = requireResource(resourceId, festival);
        Long festivalId = resource.getFestival().getFestivalId();
        if (eventResourceRepository.existsByNameIgnoreCaseAndFestival_FestivalIdAndIdNot(request.getName(), festivalId, resourceId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A resource with this name already exists");
        }
        resource.setName(request.getName());
        resource.setType(request.getType());
        resource.setDescription(request.getDescription());
        resource.setTotalQuantity(request.getTotalQuantity());
        return EventResourceResponse.from(eventResourceRepository.save(resource));
    }

    @Transactional
    public void deleteResource(Long resourceId, User user) {
        Festival festival = requireEventOrganizerFestival(user);
        requireResource(resourceId, festival);
        stageResourceRepository.deleteByResource_Id(resourceId);
        eventResourceRepository.deleteById(resourceId);
    }

    public List<StageResourceResponse> getStageResources(Long stageId, User user) {
        Festival festival = requireEventOrganizerFestival(user);
        requireStage(stageId, festival);
        return stageResourceRepository.findByStage_StageIdOrderByResource_NameAsc(stageId)
            .stream().map(StageResourceResponse::from).toList();
    }

    public StageResourceResponse assignResourceToStage(Long stageId, StageResourceRequest request, User user) {
        Festival festival = requireEventOrganizerFestival(user);
        Stage stage = requireStage(stageId, festival);
        EventResource resource = requireResource(request.getResourceId(), festival);
        if (!stage.getFestival().getFestivalId().equals(resource.getFestival().getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Resource and stage must belong to the same festival");
        }
        if (stageResourceRepository.existsByStage_StageIdAndResource_Id(stageId, request.getResourceId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This resource is already assigned to the stage");
        }
        validateQuantity(request.getQuantity(), resource);
        StageResource stageResource = StageResource.builder()
            .stage(stage)
            .resource(resource)
            .quantity(request.getQuantity())
            .build();
        return StageResourceResponse.from(stageResourceRepository.save(stageResource));
    }

    public StageResourceResponse updateStageResource(Long stageId, Long resourceId, StageResourceRequest request, User user) {
        Festival festival = requireEventOrganizerFestival(user);
        requireStage(stageId, festival);
        EventResource resource = requireResource(resourceId, festival);
        StageResource stageResource = stageResourceRepository.findByStage_StageIdAndResource_Id(stageId, resourceId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stage resource assignment was not found"));
        validateQuantity(request.getQuantity(), resource);
        stageResource.setQuantity(request.getQuantity());
        return StageResourceResponse.from(stageResourceRepository.save(stageResource));
    }

    @Transactional
    public void removeResourceFromStage(Long stageId, Long resourceId, User user) {
        Festival festival = requireEventOrganizerFestival(user);
        requireStage(stageId, festival);
        requireResource(resourceId, festival);
        if (!stageResourceRepository.existsByStage_StageIdAndResource_Id(stageId, resourceId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Stage resource assignment was not found");
        }
        stageResourceRepository.deleteByStage_StageIdAndResource_Id(stageId, resourceId);
    }

    public List<TimetableSlotResponse> getStageTimetable(Long stageId, LocalDate date, User user) {
        Festival festival = requireEventOrganizerFestival(user);
        requireStage(stageId, festival);
        List<TimetableSlotResponse> slots = new ArrayList<>();
        for (int hour = 12; hour < 24; hour++) {
            slots.add(new TimetableSlotResponse(
                date,
                LocalTime.of(hour, 0),
                hour == 23 ? LocalTime.MIDNIGHT : LocalTime.of(hour + 1, 0),
                AVAILABLE_STATUS,
                null
            ));
        }
        return slots;
    }

    private void validateQuantity(Integer quantity, EventResource resource) {
        if (quantity > resource.getTotalQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assigned quantity cannot exceed total quantity");
        }
    }
}

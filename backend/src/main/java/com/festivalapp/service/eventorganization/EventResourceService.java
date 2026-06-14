package com.festivalapp.service.eventorganization;

import com.festivalapp.dto.eventorganization.EventResourceRequest;
import com.festivalapp.dto.eventorganization.EventResourceResponse;
import com.festivalapp.model.Festival;
import com.festivalapp.model.User;
import com.festivalapp.model.eventorganization.EventResource;
import com.festivalapp.repository.eventorganization.EventResourceRepository;
import com.festivalapp.repository.eventorganization.RequestResourceRepository;
import com.festivalapp.repository.eventorganization.StageResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventResourceService {

    private final EventResourceRepository eventResourceRepository;
    private final StageResourceRepository stageResourceRepository;
    private final RequestResourceRepository requestResourceRepository;
    private final EventOrganizationAccessService accessService;

    public List<EventResourceResponse> getResources(User user) {
        Festival festival = accessService.requireEventOrganizerFestival(user);
        List<EventResource> resources = festival == null
            ? eventResourceRepository.findAll()
            : eventResourceRepository.findByFestival_FestivalIdOrderByNameAsc(festival.getFestivalId());
        return resources.stream().map(EventResourceResponse::from).toList();
    }

    public EventResourceResponse createResource(EventResourceRequest request, User user) {
        Festival festival = accessService.requireEventOrganizerFestival(user);
        if (festival == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin users must assign resources through an event organizer");
        }
        if (eventResourceRepository.findByFestival_FestivalIdAndNameIgnoreCaseAndTypeIgnoreCaseAndShareable(
            festival.getFestivalId(),
            request.getName(),
            request.getType(),
            Boolean.TRUE.equals(request.getShareable())
        ).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A resource with this name, type and sharing setting already exists");
        }
        EventResource resource = EventResource.builder()
            .name(request.getName())
            .type(request.getType())
            .description(request.getDescription())
            .totalQuantity(request.getTotalQuantity())
            .shareable(Boolean.TRUE.equals(request.getShareable()))
            .festival(festival)
            .build();
        return EventResourceResponse.from(eventResourceRepository.save(resource));
    }

    public EventResourceResponse updateResource(Long resourceId, EventResourceRequest request, User user) {
        Festival festival = accessService.requireEventOrganizerFestival(user);
        EventResource resource = accessService.requireResource(resourceId, festival);
        if (eventResourceRepository.existsByFestival_FestivalIdAndNameIgnoreCaseAndTypeIgnoreCaseAndShareableAndIdNot(
            resource.getFestival().getFestivalId(),
            request.getName(),
            request.getType(),
            Boolean.TRUE.equals(request.getShareable()),
            resourceId
        )) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A resource with this name, type and sharing setting already exists");
        }
        if (stageResourceRepository.existsDuplicateResourceNameOnAssignedStages(resourceId, request.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A resource with this name already exists on this stage");
        }
        resource.setName(request.getName());
        resource.setType(request.getType());
        resource.setDescription(request.getDescription());
        resource.setTotalQuantity(resolveTotalQuantity(resourceId, request.getTotalQuantity()));
        resource.setShareable(Boolean.TRUE.equals(request.getShareable()));
        return EventResourceResponse.from(eventResourceRepository.save(resource));
    }

    @Transactional
    public void deleteResource(Long resourceId, User user) {
        Festival festival = accessService.requireEventOrganizerFestival(user);
        accessService.requireResource(resourceId, festival);
        requestResourceRepository.deleteByResource_Id(resourceId);
        stageResourceRepository.deleteByResource_Id(resourceId);
        eventResourceRepository.deleteById(resourceId);
    }

    private Integer resolveTotalQuantity(Long resourceId, Integer fallbackQuantity) {
        Long assignedQuantity = stageResourceRepository.sumQuantityByResourceId(resourceId);
        return assignedQuantity > 0 ? assignedQuantity.intValue() : fallbackQuantity;
    }
}

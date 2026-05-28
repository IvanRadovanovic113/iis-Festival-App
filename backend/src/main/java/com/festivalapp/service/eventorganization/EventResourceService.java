package com.festivalapp.service.eventorganization;

import com.festivalapp.dto.eventorganization.EventResourceRequest;
import com.festivalapp.dto.eventorganization.EventResourceResponse;
import com.festivalapp.model.Festival;
import com.festivalapp.model.User;
import com.festivalapp.model.eventorganization.EventResource;
import com.festivalapp.repository.eventorganization.EventResourceRepository;
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
        Festival festival = accessService.requireEventOrganizerFestival(user);
        EventResource resource = accessService.requireResource(resourceId, festival);
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
        Festival festival = accessService.requireEventOrganizerFestival(user);
        accessService.requireResource(resourceId, festival);
        stageResourceRepository.deleteByResource_Id(resourceId);
        eventResourceRepository.deleteById(resourceId);
    }
}

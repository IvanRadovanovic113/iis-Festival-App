package com.festivalapp.service.eventorganization;

import com.festivalapp.dto.eventorganization.StageResourceRequest;
import com.festivalapp.dto.eventorganization.StageResourceResponse;
import com.festivalapp.model.Festival;
import com.festivalapp.model.Stage;
import com.festivalapp.model.User;
import com.festivalapp.model.eventorganization.EventResource;
import com.festivalapp.model.eventorganization.StageResource;
import com.festivalapp.repository.eventorganization.StageResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StageResourceService {

    private final StageResourceRepository stageResourceRepository;
    private final EventOrganizationAccessService accessService;

    public List<StageResourceResponse> getStageResources(Long stageId, User user) {
        Festival festival = accessService.requireEventOrganizerFestival(user);
        accessService.requireStage(stageId, festival);
        return stageResourceRepository.findByStage_StageIdOrderByResource_NameAsc(stageId)
            .stream().map(StageResourceResponse::from).toList();
    }

    public StageResourceResponse assignResourceToStage(Long stageId, StageResourceRequest request, User user) {
        Festival festival = accessService.requireEventOrganizerFestival(user);
        Stage stage = accessService.requireStage(stageId, festival);
        EventResource resource = accessService.requireResource(request.getResourceId(), festival);
        if (!stage.getFestival().getFestivalId().equals(resource.getFestival().getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Resource and stage must belong to the same festival");
        }
        if (stageResourceRepository.existsByStage_StageIdAndResource_Id(stageId, request.getResourceId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This resource is already assigned to the stage");
        }
        if (stageResourceRepository.existsByStage_StageIdAndResource_NameIgnoreCase(stageId, resource.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A resource with this name already exists on this stage");
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
        Festival festival = accessService.requireEventOrganizerFestival(user);
        accessService.requireStage(stageId, festival);
        EventResource resource = accessService.requireResource(resourceId, festival);
        StageResource stageResource = stageResourceRepository.findByStage_StageIdAndResource_Id(stageId, resourceId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stage resource assignment was not found"));
        validateQuantity(request.getQuantity(), resource);
        stageResource.setQuantity(request.getQuantity());
        return StageResourceResponse.from(stageResourceRepository.save(stageResource));
    }

    @Transactional
    public void removeResourceFromStage(Long stageId, Long resourceId, User user) {
        Festival festival = accessService.requireEventOrganizerFestival(user);
        accessService.requireStage(stageId, festival);
        accessService.requireResource(resourceId, festival);
        if (!stageResourceRepository.existsByStage_StageIdAndResource_Id(stageId, resourceId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Stage resource assignment was not found");
        }
        stageResourceRepository.deleteByStage_StageIdAndResource_Id(stageId, resourceId);
    }

    private void validateQuantity(Integer quantity, EventResource resource) {
        if (quantity > resource.getTotalQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assigned quantity cannot exceed total quantity");
        }
    }
}

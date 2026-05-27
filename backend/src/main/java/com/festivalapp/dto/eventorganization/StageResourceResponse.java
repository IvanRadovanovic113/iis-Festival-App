package com.festivalapp.dto.eventorganization;

import com.festivalapp.model.eventorganization.StageResource;

public record StageResourceResponse(
    Long id,
    Long stageId,
    Long resourceId,
    String resourceName,
    String resourceType,
    Integer quantity,
    Integer totalQuantity
) {
    public static StageResourceResponse from(StageResource stageResource) {
        return new StageResourceResponse(
            stageResource.getId(),
            stageResource.getStage().getStageId(),
            stageResource.getResource().getId(),
            stageResource.getResource().getName(),
            stageResource.getResource().getType(),
            stageResource.getQuantity(),
            stageResource.getResource().getTotalQuantity()
        );
    }
}

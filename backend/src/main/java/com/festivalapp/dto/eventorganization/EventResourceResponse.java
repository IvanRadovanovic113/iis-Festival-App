package com.festivalapp.dto.eventorganization;

import com.festivalapp.model.eventorganization.EventResource;

public record EventResourceResponse(
    Long id,
    String name,
    String type,
    String description,
    Integer totalQuantity,
    Long festivalId
) {
    public static EventResourceResponse from(EventResource resource) {
        return new EventResourceResponse(
            resource.getId(),
            resource.getName(),
            resource.getType(),
            resource.getDescription(),
            resource.getTotalQuantity(),
            resource.getFestival().getFestivalId()
        );
    }
}

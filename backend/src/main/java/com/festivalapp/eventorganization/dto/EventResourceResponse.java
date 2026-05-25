package com.festivalapp.eventorganization.dto;

import com.festivalapp.eventorganization.model.EventResource;

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

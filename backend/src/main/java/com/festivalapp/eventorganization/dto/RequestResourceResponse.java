package com.festivalapp.eventorganization.dto;

import com.festivalapp.eventorganization.model.RequestResource;
import com.festivalapp.eventorganization.model.RequestResourceStatus;

public record RequestResourceResponse(
    Long id,
    Long reservationRequestId,
    Long resourceId,
    String resourceName,
    String resourceType,
    Integer quantity,
    Integer totalQuantity,
    RequestResourceStatus status
) {
    public static RequestResourceResponse from(RequestResource requestResource) {
        return new RequestResourceResponse(
            requestResource.getId(),
            requestResource.getReservationRequest().getId(),
            requestResource.getResource().getId(),
            requestResource.getResource().getName(),
            requestResource.getResource().getType(),
            requestResource.getQuantity(),
            requestResource.getResource().getTotalQuantity(),
            requestResource.getStatus()
        );
    }
}

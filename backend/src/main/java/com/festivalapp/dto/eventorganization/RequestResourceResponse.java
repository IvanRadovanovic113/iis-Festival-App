package com.festivalapp.dto.eventorganization;

import com.festivalapp.model.eventorganization.RequestResource;
import com.festivalapp.model.eventorganization.RequestResourceStatus;

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
        boolean customRequest = requestResource.getResource() == null;
        return new RequestResourceResponse(
            requestResource.getId(),
            requestResource.getReservationRequest().getId(),
            customRequest ? null : requestResource.getResource().getId(),
            customRequest ? requestResource.getRequestedName() : requestResource.getResource().getName(),
            customRequest ? requestResource.getRequestedType() : requestResource.getResource().getType(),
            requestResource.getQuantity(),
            customRequest ? null : requestResource.getResource().getTotalQuantity(),
            requestResource.getStatus()
        );
    }
}

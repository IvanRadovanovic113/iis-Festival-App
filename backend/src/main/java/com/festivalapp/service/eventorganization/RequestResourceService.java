package com.festivalapp.service.eventorganization;

import com.festivalapp.dto.eventorganization.RequestResourceRequest;
import com.festivalapp.dto.eventorganization.RequestResourceResponse;
import com.festivalapp.model.Festival;
import com.festivalapp.model.User;
import com.festivalapp.model.eventorganization.EventReservationRequest;
import com.festivalapp.model.eventorganization.EventReservationStatus;
import com.festivalapp.model.eventorganization.EventResource;
import com.festivalapp.model.eventorganization.RequestResource;
import com.festivalapp.model.eventorganization.RequestResourceStatus;
import com.festivalapp.repository.eventorganization.RequestResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestResourceService {

    private final RequestResourceRepository requestResourceRepository;
    private final EventOrganizationAccessService accessService;

    public List<RequestResourceResponse> getRequestResources(Long requestId, User user) {
        Festival festival = accessService.requireEventOrganizerFestival(user);
        accessService.requireReservationRequest(requestId, festival);
        return requestResourceRepository.findByReservationRequest_IdOrderByResource_NameAsc(requestId)
            .stream().map(RequestResourceResponse::from).toList();
    }

    public RequestResourceResponse addResourceToRequest(Long requestId, RequestResourceRequest request, User user) {
        Festival festival = accessService.requireEventOrganizerFestival(user);
        EventReservationRequest reservationRequest = accessService.requireReservationRequest(requestId, festival);
        EventResource resource = accessService.requireResource(request.getResourceId(), festival);
        validateQuantity(request.getQuantity(), resource);
        if (requestResourceRepository.existsByReservationRequest_IdAndResource_Id(requestId, request.getResourceId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This resource is already requested for the reservation");
        }
        RequestResource requestResource = RequestResource.builder()
            .reservationRequest(reservationRequest)
            .resource(resource)
            .quantity(request.getQuantity())
            .status(RequestResourceStatus.REQUESTED)
            .build();
        return RequestResourceResponse.from(requestResourceRepository.save(requestResource));
    }

    public RequestResourceResponse updateRequestResource(Long requestId, Long resourceId, RequestResourceRequest request, User user) {
        Festival festival = accessService.requireEventOrganizerFestival(user);
        RequestResource requestResource = requireRequestResource(requestId, resourceId, festival);
        EventResource resource = accessService.requireResource(resourceId, festival);
        validateQuantity(request.getQuantity(), resource);
        requestResource.setQuantity(request.getQuantity());
        requestResource.setStatus(RequestResourceStatus.REQUESTED);
        return RequestResourceResponse.from(requestResourceRepository.save(requestResource));
    }

    public RequestResourceResponse confirmRequestResource(Long requestId, Long resourceId, User user) {
        Festival festival = accessService.requireEventOrganizerFestival(user);
        RequestResource requestResource = requireRequestResource(requestId, resourceId, festival);
        EventReservationRequest reservationRequest = requestResource.getReservationRequest();
        if (reservationRequest.getStatus() == EventReservationStatus.REJECTED || reservationRequest.getStatus() == EventReservationStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resources cannot be confirmed for rejected or cancelled requests");
        }
        Integer overlappingQuantity = requestResourceRepository.sumOverlappingQuantityByResource(
            resourceId,
            requestId,
            reservationRequest.getPerformanceDate(),
            reservationRequest.getStartTime(),
            reservationRequest.getEndTime(),
            RequestResourceStatus.CONFIRMED
        );
        int availableQuantity = requestResource.getResource().getTotalQuantity() - overlappingQuantity;
        if (requestResource.getQuantity() > availableQuantity) {
            requestResource.setStatus(RequestResourceStatus.UNAVAILABLE);
            requestResourceRepository.save(requestResource);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Requested quantity is not available in this time slot");
        }
        requestResource.setStatus(RequestResourceStatus.CONFIRMED);
        return RequestResourceResponse.from(requestResourceRepository.save(requestResource));
    }

    @Transactional
    public void removeResourceFromRequest(Long requestId, Long resourceId, User user) {
        Festival festival = accessService.requireEventOrganizerFestival(user);
        requireRequestResource(requestId, resourceId, festival);
        requestResourceRepository.deleteByReservationRequest_IdAndResource_Id(requestId, resourceId);
    }

    private RequestResource requireRequestResource(Long requestId, Long resourceId, Festival festival) {
        accessService.requireReservationRequest(requestId, festival);
        return requestResourceRepository.findByReservationRequest_IdAndResource_Id(requestId, resourceId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request resource was not found"));
    }

    private void validateQuantity(Integer quantity, EventResource resource) {
        if (quantity > resource.getTotalQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assigned quantity cannot exceed total quantity");
        }
    }
}

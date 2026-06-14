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
        RequestResource.RequestResourceBuilder builder = RequestResource.builder()
            .reservationRequest(reservationRequest)
            .quantity(request.getQuantity())
            .status(RequestResourceStatus.REQUESTED);

        if (request.getResourceId() != null) {
            EventResource resource = accessService.requireResource(request.getResourceId(), festival);
            validateQuantity(request.getQuantity(), resource);
            if (requestResourceRepository.existsByReservationRequest_IdAndResource_Id(requestId, request.getResourceId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "This resource is already requested for the reservation");
            }
            builder.resource(resource);
        } else {
            String requestedName = requireText(request.getRequestedName(), "Requested resource name is required");
            String requestedType = requireText(request.getRequestedType(), "Requested resource type is required");
            if (requestResourceRepository.existsByReservationRequest_IdAndRequestedNameIgnoreCase(requestId, requestedName)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "This custom resource is already requested for the reservation");
            }
            builder.requestedName(requestedName)
                .requestedType(requestedType);
        }

        return RequestResourceResponse.from(requestResourceRepository.save(builder.build()));
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

    public RequestResourceResponse updateRequestResourceItem(Long requestId, Long requestResourceId, RequestResourceRequest request, User user) {
        Festival festival = accessService.requireEventOrganizerFestival(user);
        RequestResource requestResource = requireRequestResourceItem(requestId, requestResourceId, festival);

        if (request.getResourceId() != null) {
            EventResource resource = accessService.requireResource(request.getResourceId(), festival);
            validateQuantity(request.getQuantity(), resource);
            requestResource.setResource(resource);
            requestResource.setRequestedName(null);
            requestResource.setRequestedType(null);
        } else {
            requestResource.setResource(null);
            requestResource.setRequestedName(requireText(request.getRequestedName(), "Requested resource name is required"));
            requestResource.setRequestedType(requireText(request.getRequestedType(), "Requested resource type is required"));
        }

        requestResource.setQuantity(request.getQuantity());
        requestResource.setStatus(RequestResourceStatus.REQUESTED);
        return RequestResourceResponse.from(requestResourceRepository.save(requestResource));
    }

    public RequestResourceResponse confirmRequestResourceItem(Long requestId, Long requestResourceId, User user) {
        Festival festival = accessService.requireEventOrganizerFestival(user);
        RequestResource requestResource = requireRequestResourceItem(requestId, requestResourceId, festival);
        if (requestResource.getResource() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Custom requested resources cannot be confirmed from inventory");
        }
        return confirmExistingRequestResource(requestResource);
    }

    public RequestResourceResponse confirmRequestResource(Long requestId, Long resourceId, User user) {
        Festival festival = accessService.requireEventOrganizerFestival(user);
        RequestResource requestResource = requireRequestResource(requestId, resourceId, festival);
        return confirmExistingRequestResource(requestResource);
    }

    private RequestResourceResponse confirmExistingRequestResource(RequestResource requestResource) {
        EventReservationRequest reservationRequest = requestResource.getReservationRequest();
        if (reservationRequest.getStatus() == EventReservationStatus.REJECTED || reservationRequest.getStatus() == EventReservationStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resources cannot be confirmed for rejected or cancelled requests");
        }
        Long resourceId = requestResource.getResource().getId();
        Integer overlappingQuantity = requestResourceRepository.sumOverlappingQuantityByResource(
            resourceId,
            reservationRequest.getId(),
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

    @Transactional
    public void removeRequestResourceItem(Long requestId, Long requestResourceId, User user) {
        Festival festival = accessService.requireEventOrganizerFestival(user);
        RequestResource requestResource = requireRequestResourceItem(requestId, requestResourceId, festival);
        requestResourceRepository.delete(requestResource);
    }

    private RequestResource requireRequestResource(Long requestId, Long resourceId, Festival festival) {
        accessService.requireReservationRequest(requestId, festival);
        return requestResourceRepository.findByReservationRequest_IdAndResource_Id(requestId, resourceId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request resource was not found"));
    }

    private RequestResource requireRequestResourceItem(Long requestId, Long requestResourceId, Festival festival) {
        accessService.requireReservationRequest(requestId, festival);
        return requestResourceRepository.findByIdAndReservationRequest_Id(requestResourceId, requestId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request resource was not found"));
    }

    private void validateQuantity(Integer quantity, EventResource resource) {
        if (quantity > resource.getTotalQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assigned quantity cannot exceed total quantity");
        }
    }

    private String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }
}

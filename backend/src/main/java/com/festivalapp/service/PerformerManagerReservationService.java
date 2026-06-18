package com.festivalapp.service;

import com.festivalapp.dto.eventorganization.EventReservationResponse;
import com.festivalapp.dto.performermanager.ContractReservationCustomResourceRequest;
import com.festivalapp.dto.performermanager.ContractReservationResourceRequest;
import com.festivalapp.dto.performermanager.CreateContractReservationRequest;
import com.festivalapp.dto.performermanager.PerformerContractReservationResponse;
import com.festivalapp.model.Contract;
import com.festivalapp.model.Festival;
import com.festivalapp.model.NegotiationStatus;
import com.festivalapp.model.Role;
import com.festivalapp.model.User;
import com.festivalapp.model.UserFestivalAssignment;
import com.festivalapp.model.eventorganization.EventResource;
import com.festivalapp.model.eventorganization.EventReservationRequest;
import com.festivalapp.model.eventorganization.EventReservationStatus;
import com.festivalapp.model.eventorganization.RequestResource;
import com.festivalapp.model.eventorganization.RequestResourceStatus;
import com.festivalapp.model.eventorganization.StageResource;
import com.festivalapp.repository.ContractRepository;
import com.festivalapp.repository.UserFestivalAssignmentRepository;
import com.festivalapp.repository.eventorganization.EventResourceRepository;
import com.festivalapp.repository.eventorganization.EventReservationRequestRepository;
import com.festivalapp.repository.eventorganization.RequestResourceRepository;
import com.festivalapp.repository.eventorganization.StageResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PerformerManagerReservationService {

    private final ContractRepository contractRepository;
    private final EventReservationRequestRepository reservationRequestRepository;
    private final RequestResourceRepository requestResourceRepository;
    private final EventResourceRepository eventResourceRepository;
    private final StageResourceRepository stageResourceRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;

    @Transactional(readOnly = true)
    public List<PerformerContractReservationResponse> getContracts(User user) {
        Festival festival = requirePerformerManagerFestival(user);

        List<EventResource> allShareable = eventResourceRepository
            .findByFestival_FestivalIdAndShareableTrueOrderByNameAsc(festival.getFestivalId());

        return contractRepository.findCompletedStageAssignedByFestival(festival.getFestivalId()).stream()
            .map(contract -> {
                List<StageResource> stageResources = stageResourceRepository
                    .findByStage_StageIdOrderByResource_NameAsc(contract.getStage().getStageId());
                Set<Long> stageResourceIds = stageResources.stream()
                    .map(sr -> sr.getResource().getId())
                    .collect(Collectors.toSet());
                List<EventResource> additionalShareable = allShareable.stream()
                    .filter(r -> !stageResourceIds.contains(r.getId()))
                    .toList();
                return PerformerContractReservationResponse.from(
                    contract,
                    reservationRequestRepository.findByContract_Id(contract.getId()).orElse(null),
                    stageResources,
                    additionalShareable
                );
            })
            .toList();
    }

    @Transactional
    public EventReservationResponse createReservationRequest(Long contractId, CreateContractReservationRequest request, User user) {
        Festival festival = requirePerformerManagerFestival(user);
        Contract contract = requireContract(contractId, festival);

        if (reservationRequestRepository.existsByContract_Id(contractId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reservation request already exists for this contract");
        }

        LocalDateTime performanceDateTime = contract.getNegotiation().getOffer().getPerformanceDate();
        int durationMinutes = contract.getNegotiation().getOffer().getDurationMinutes();
        if (durationMinutes <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contract duration is invalid");
        }

        LocalTime startTime = performanceDateTime.toLocalTime();
        LocalTime endTime = startTime.plusMinutes(durationMinutes);
        if (!startTime.isBefore(endTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contract performance time is invalid");
        }

        EventReservationRequest reservationRequest = EventReservationRequest.builder()
            .festival(festival)
            .performerName(contract.getNegotiation().getPerformer().getStageName())
            .stage(contract.getStage())
            .contract(contract)
            .performanceDate(performanceDateTime.toLocalDate())
            .startTime(startTime)
            .endTime(endTime)
            .status(EventReservationStatus.PENDING)
            .notes(blankToNull(request.getNotes()))
            .build();

        EventReservationRequest savedRequest = reservationRequestRepository.save(reservationRequest);
        saveRequestedResources(savedRequest, request);
        return EventReservationResponse.from(savedRequest);
    }

    private void saveRequestedResources(EventReservationRequest reservationRequest, CreateContractReservationRequest request) {
        Festival festival = reservationRequest.getFestival();
        Set<Long> existingResourceIds = new HashSet<>();
        for (ContractReservationResourceRequest resourceRequest : safeExistingResources(request)) {
            if (!existingResourceIds.add(resourceRequest.getResourceId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Resource is requested more than once");
            }

            EventResource resource = eventResourceRepository.findById(resourceRequest.getResourceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resource not found"));

            if (!resource.getFestival().getFestivalId().equals(festival.getFestivalId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Resource does not belong to your festival");
            }
            boolean isOnStage = stageResourceRepository.existsByStage_StageIdAndResource_Id(
                reservationRequest.getStage().getStageId(), resource.getId());
            if (!isOnStage && !Boolean.TRUE.equals(resource.getShareable())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resource is not available for this reservation");
            }
            if (resourceRequest.getQuantity() < 1 || resourceRequest.getQuantity() > resource.getTotalQuantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Quantity must be between 1 and " + resource.getTotalQuantity());
            }

            requestResourceRepository.save(RequestResource.builder()
                .reservationRequest(reservationRequest)
                .resource(resource)
                .quantity(resourceRequest.getQuantity())
                .status(RequestResourceStatus.REQUESTED)
                .build());
        }

        Set<String> customResourceNames = new HashSet<>();
        for (ContractReservationCustomResourceRequest customRequest : safeCustomResources(request)) {
            String requestedName = customRequest.getRequestedName().trim();
            if (!customResourceNames.add(requestedName.toLowerCase())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Custom resource is requested more than once");
            }

            requestResourceRepository.save(RequestResource.builder()
                .reservationRequest(reservationRequest)
                .requestedName(requestedName)
                .requestedType(customRequest.getRequestedType().trim())
                .quantity(customRequest.getQuantity())
                .status(RequestResourceStatus.REQUESTED)
                .build());
        }
    }

    private Contract requireContract(Long contractId, Festival festival) {
        Contract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contract not found"));
        if (contract.getStage() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contract must have an assigned stage before creating a reservation request");
        }
        if (contract.getNegotiation().getStatus() != NegotiationStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only completed contracts can create reservation requests");
        }
        if (!contract.getStage().getFestival().getFestivalId().equals(festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Contract does not belong to your festival");
        }
        return contract;
    }

    private Festival requirePerformerManagerFestival(User user) {
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Festival assignment is required"));
        if (assignment.getRole() != Role.PERFORMER_MANAGER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Performer manager role is required");
        }
        return assignment.getFestival();
    }

    private List<ContractReservationResourceRequest> safeExistingResources(CreateContractReservationRequest request) {
        return request.getExistingResources() == null ? List.of() : request.getExistingResources();
    }

    private List<ContractReservationCustomResourceRequest> safeCustomResources(CreateContractReservationRequest request) {
        return request.getCustomResources() == null ? List.of() : request.getCustomResources();
    }

    private String blankToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}

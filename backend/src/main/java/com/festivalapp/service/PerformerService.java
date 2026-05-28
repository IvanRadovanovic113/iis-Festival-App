package com.festivalapp.service;

import com.festivalapp.dto.PerformerRequest;
import com.festivalapp.dto.PerformerResponse;
import com.festivalapp.model.Performer;
import com.festivalapp.model.PerformerStatus;
import com.festivalapp.model.PerformerType;
import com.festivalapp.model.Role;
import com.festivalapp.model.User;
import com.festivalapp.model.UserFestivalAssignment;
import com.festivalapp.repository.PerformerRepository;
import com.festivalapp.repository.UserFestivalAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PerformerService {

    private final PerformerRepository performerRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;

    private void requireNegotiationManager(Long userId) {
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Festival assignment is required"));
        
        if (assignment.getRole() != Role.NEGOTIATION_MANAGER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only negotiation managers can manage performers");
        }
    }

    private void validatePerformerTypeAndMembers(PerformerType type, Integer numberOfMembers) {
        if (type == PerformerType.SOLO && numberOfMembers != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A SOLO performer must have exactly 1 member.");
        }
        if (type == PerformerType.BAND && numberOfMembers <= 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A BAND must have more than 1 member.");
        }
    }

    // kreiranje izvodjaca (Jira Kartica FEST-145)
    @Transactional
    public PerformerResponse createPerformer(PerformerRequest request, User user) {
        requireNegotiationManager(user.getId());

        if (performerRepository.existsByStageNameIgnoreCase(request.getStageName().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Performer with this stage name already exists.");
        }

        validatePerformerTypeAndMembers(request.getPerformerType(), request.getNumberOfMembers());

        Performer performer = Performer.builder()
            .stageName(request.getStageName().trim())
            .firstName(request.getFirstName() != null ? request.getFirstName().trim() : null)
            .lastName(request.getLastName() != null ? request.getLastName().trim() : null)
            .genre(request.getGenre().trim())
            .popularity(request.getPopularity())
            .averageDurationMinutes(request.getAverageDurationMinutes())
            .countryOfOrigin(request.getCountryOfOrigin().trim())
            .performerType(request.getPerformerType())
            .numberOfMembers(request.getNumberOfMembers())
            .status(PerformerStatus.ACTIVE)
            .bio(request.getBio() != null ? request.getBio().trim() : null)
            .build();

        return PerformerResponse.from(performerRepository.save(performer));
    }

    // pregled, pretraga i filtriranje izvodjaca (Jira Kartice FEST-146, FEST-147)
    @Transactional(readOnly = true)
    public Page<PerformerResponse> getPerformers(
            PerformerStatus status,
            String genre,
            PerformerType performerType,
            String countryOfOrigin,
            Integer numberOfMembers,
            String searchName,
            Pageable pageable,
            User user) {
        
        requireNegotiationManager(user.getId());

        PerformerStatus statusFilter = (status != null) ? status : PerformerStatus.ACTIVE;

        Page<Performer> performersPage = performerRepository.findByFilters(
                statusFilter,
                genre,
                performerType,
                countryOfOrigin,
                numberOfMembers,
                searchName,
                pageable
        );

        return performersPage.map(PerformerResponse::from);
    }

    // pregled detalja izvodjaca (Jira Kartica FEST-148)
    @Transactional(readOnly = true)
    public PerformerResponse getPerformerById(Long performerId, User user) {
        requireNegotiationManager(user.getId());

        Performer performer = performerRepository.findById(performerId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Performer not found."));

        return PerformerResponse.from(performer);
    }

    // izmena izvodjaca (Jira Kartica FEST-149)
    @Transactional
    public PerformerResponse updatePerformer(Long performerId, PerformerRequest request, User user) {
        requireNegotiationManager(user.getId());

        Performer performer = performerRepository.findById(performerId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Performer not found."));

        if (performerRepository.existsByStageNameIgnoreCaseAndPerformerIdNot(request.getStageName().trim(), performerId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Another performer already uses this stage name.");
        }

        validatePerformerTypeAndMembers(request.getPerformerType(), request.getNumberOfMembers());

        performer.setStageName(request.getStageName().trim());
        performer.setFirstName(request.getFirstName() != null ? request.getFirstName().trim() : null);
        performer.setLastName(request.getLastName() != null ? request.getLastName().trim() : null);
        performer.setGenre(request.getGenre().trim());
        performer.setPopularity(request.getPopularity());
        performer.setAverageDurationMinutes(request.getAverageDurationMinutes());
        performer.setCountryOfOrigin(request.getCountryOfOrigin().trim());
        performer.setPerformerType(request.getPerformerType());
        performer.setNumberOfMembers(request.getNumberOfMembers());
        performer.setBio(request.getBio() != null ? request.getBio().trim() : null);

        return PerformerResponse.from(performerRepository.save(performer));
    }

    // arhiviranje izvodjaca (Jira Kartica FEST-150)
    @Transactional
    public PerformerResponse archivePerformer(Long performerId, User user) {
        requireNegotiationManager(user.getId());

        Performer performer = performerRepository.findById(performerId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Performer not found."));

        if (performer.getStatus() == PerformerStatus.ARCHIVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Performer is already archived.");
        }

        performer.setStatus(PerformerStatus.ARCHIVED);

        return PerformerResponse.from(performerRepository.save(performer));
    }
}
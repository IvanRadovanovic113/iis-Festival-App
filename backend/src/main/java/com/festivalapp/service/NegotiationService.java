package com.festivalapp.service;

import com.festivalapp.dto.TransitionRequest;
import com.festivalapp.dto.ConditionValueDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import com.festivalapp.model.*;
import com.festivalapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Collectors;
import com.festivalapp.repository.NegotiationSpecification;
import com.festivalapp.dto.TransitionDto;
import com.festivalapp.dto.TransitionConditionResponse;
import com.festivalapp.dto.NegotiationMapper;
import com.festivalapp.dto.NegotiationDetailsResponse;
import com.festivalapp.dto.FailReasonRequest;
import java.util.ArrayList;
import java.util.Map;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NegotiationService {

    private final NegotiationRepository negotiationRepository;
    private final NegotiationStateHistoryRepository historyRepository;
    private final OfferRepository offerRepository;
    private final PerformerRepository performerRepository;
    private final WorkflowTemplateRepository workflowTemplateRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;
    private final WorkflowTransitionRepository workflowTransitionRepository;
    private final NegotiationConditionValueRepository negotiationConditionValueRepository;
    private final TransitionConditionRepository transitionConditionRepository;
    private final ContractRepository contractRepository;

    private void requireNegotiationManager(Long userId) {
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Festival assignment required"));
        
        if (assignment.getRole() != Role.NEGOTIATION_MANAGER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only negotiation managers can perform this action");
        }
    }

    @Transactional
    public Negotiation initiateNegotiation(Long offerId, Long performerId, User user) {
        requireNegotiationManager(user.getId());

        Offer offer = offerRepository.findById(offerId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));

        Performer performer = performerRepository.findById(performerId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Performer not found"));

        if (offer.getStatus() != OfferStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PUBLISHED offers can initiate a negotiation");
        }

        // Dohvatanje šablona i njegovog početnog stanja
        WorkflowTemplate template = workflowTemplateRepository.findById(offer.getWorkflowTemplateId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found"));
            
        WorkflowState initialState = template.getStates().stream()
            .filter(WorkflowState::isInitial) // Koristimo getter generisan od strane Lomboka za boolean polje
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No initial state defined in template"));

        // 1. Kreiranje pregovora
        Negotiation negotiation = Negotiation.builder()
            .offer(offer)
            .performer(performer)
            .startedBy(user)
            .currentState(initialState)
            .status(NegotiationStatus.ACTIVE)
            .build();
        negotiation = negotiationRepository.save(negotiation);

        // 2. Zamrzavanje ponude
        offer.setStatus(OfferStatus.FROZEN);
        offer.setFrozenAt(LocalDateTime.now());
        offerRepository.save(offer);

        // 3. Inicijalizacija istorije stanja
        NegotiationStateHistory history = new NegotiationStateHistory();
        history.setNegotiation(negotiation);
        history.setState(initialState);
        history.setEntryTime(LocalDateTime.now());
        historyRepository.save(history);

        return negotiation;
    }

    @Transactional(readOnly = true)
    public NegotiationDetailsResponse getNegotiationDetails(Long negotiationId, User user) {
        Negotiation negotiation = negotiationRepository.findById(negotiationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Negotiation not found"));

        if (!negotiation.getStartedBy().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this negotiation");
        }

        var history = historyRepository.findByNegotiation_IdOrderByEntryTimeDesc(negotiationId);

        var allConditions = negotiationConditionValueRepository.findByNegotiation_Id(negotiationId);
        var uniqueConditions = new ArrayList<>(allConditions.stream()
                .collect(Collectors.toMap(
                        c -> c.getCondition().getId(),
                        c -> c,
                        (existing, replacement) -> existing.getEnteredAt().isAfter(replacement.getEnteredAt()) 
                                                ? existing : replacement
                )).values());

        List<WorkflowTransition> possible = workflowTransitionRepository.findBySourceState_Id(negotiation.getCurrentState().getId());

        List<TransitionDto> transitionDtos = possible.stream().map(t -> TransitionDto.builder()
                .id(t.getId())
                .label(t.getLabel())
                .requiredConditions(t.getConditions().stream()
                        .map(TransitionConditionResponse::from).collect(Collectors.toList()))
                .build()).collect(Collectors.toList());

        return NegotiationMapper.toDetailsResponse(negotiation, history, uniqueConditions, transitionDtos);
    }

    @Transactional
    public void transitionToNextState(Long negotiationId, TransitionRequest request, User user) {
        Negotiation negotiation = negotiationRepository.findById(negotiationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Negotiation not found"));

        // 1. Provera prava (samo vlasnik pregovora)
        if (!negotiation.getStartedBy().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        // 2. Pronalazak tranzicije
        WorkflowTransition transition = workflowTransitionRepository.findById(request.getTransitionId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transition not found"));

        // 3. Validacija: Da li je tranzicija moguća iz trenutnog stanja?
        if (!transition.getSourceState().getId().equals(negotiation.getCurrentState().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid transition from current state");
        }

        // Validacija: Da li su svi obavezni uslovi već sačuvani u bazi za ovaj pregovor?
        List<NegotiationConditionValue> existingValues = negotiationConditionValueRepository.findByNegotiation_Id(negotiationId);
        
        for (TransitionCondition condition : transition.getConditions()) {
            if (condition.isNecessary()) {
                boolean isSet = existingValues.stream()
                    .anyMatch(cv -> cv.getCondition().getId().equals(condition.getId()));
                
                if (!isSet) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Cannot transition: Mandatory condition " + condition.getLabel() + " is not set.");
                }
            }
        }

        // 6. Ažuriranje istorije: zatvaranje trenutnog stanja
        NegotiationStateHistory currentHistory = historyRepository
                .findByNegotiation_IdAndExitTimeIsNull(negotiation.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No active history record"));
        
        currentHistory.setExitTime(LocalDateTime.now());
        historyRepository.save(currentHistory);

        // 7. Otvaranje novog stanja u istoriji i ažuriranje pregovora
        negotiation.setCurrentState(transition.getTargetState());
        negotiationRepository.save(negotiation);

        NegotiationStateHistory newHistory = new NegotiationStateHistory();
        newHistory.setNegotiation(negotiation);
        newHistory.setState(transition.getTargetState());
        newHistory.setEntryTime(LocalDateTime.now());
        historyRepository.save(newHistory);
    }

    @Transactional
    public void saveNegotiationConditions(Long negotiationId, List<ConditionValueDto> conditionValues, User user) {
        Negotiation negotiation = negotiationRepository.findById(negotiationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Negotiation not found"));

        if (!negotiation.getStartedBy().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        for (ConditionValueDto cv : conditionValues) {
            TransitionCondition condition = transitionConditionRepository.findById(cv.getConditionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Condition not found"));

            // Provera: Da li već postoji vrednost za ovaj uslov u ovom pregovoru?
            // Ako postoji, ažuriramo je, ako ne, kreiramo novu.
            
            NegotiationConditionValue ncv = new NegotiationConditionValue(
                null, negotiation, condition, cv.getValue(), LocalDateTime.now()
            );
            negotiationConditionValueRepository.save(ncv);
        }
    }

    @Transactional
    public void completeNegotiation(Long negotiationId, User user) {
        // 1. Provera ulaza
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        // 2. Osvežavanje pregovora iz baze (osigurava da je u sesiji)
        Negotiation negotiation = negotiationRepository.findById(negotiationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Negotiation not found"));

            System.out.println("=== COMPLETE DEBUG ===");
    System.out.println("user.getId() = " + user.getId());
    System.out.println("startedBy.getId() = " + negotiation.getStartedBy().getId());
    System.out.println("equals = " + negotiation.getStartedBy().getId().equals(user.getId()));
    System.out.println("======================");

        // 3. Sigurnosna provera
        if (!negotiation.getStartedBy().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        // 4. Provera statusa pregovora (mora biti u finalnom stanju)
        if (negotiation.getCurrentState() == null || !negotiation.getCurrentState().isFinalState()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Negotiation not in final state.");
        }
        
        // 5. Provera da li već postoji ugovor da izbegnemo grešku baze
        // Ako bi ovo vratilo true, ne smemo praviti dupli ugovor
        // (Ovo je opcionalno, zavisi da li dozvoljavaš ponovno potpisivanje)
        
        // 6. Priprema podataka za Snapshot
        List<NegotiationConditionValue> allConditions = negotiationConditionValueRepository.findByNegotiation_Id(negotiationId);
        String snapshotJson = convertConditionsToJson(allConditions);

        // 7. Kreiranje i čuvanje Ugovora
        Contract contract = new Contract();
        contract.setNegotiation(negotiation);
        contract.setSignedBy(user);
        contract.setConditionSnapshotJson(snapshotJson);
        contract.setSignedAt(LocalDateTime.now());
        
        contractRepository.save(contract);

        // 8. Status pregovora -> COMPLETED
        negotiation.setStatus(NegotiationStatus.COMPLETED);
        negotiationRepository.save(negotiation);

        // 9. Status ponude -> ACCEPTED
        Offer offer = negotiation.getOffer();
        if (offer != null) {
            offer.setStatus(OfferStatus.ACCEPTED);
            offer.setAcceptedAt(LocalDateTime.now());
            offerRepository.save(offer);
        }
    }

    private String convertConditionsToJson(List<NegotiationConditionValue> conditions) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> snapshot = conditions.stream()
                .collect(Collectors.toMap(
                    c -> c.getCondition().getLabel(),
                    NegotiationConditionValue::getValue,
                    (existing, replacement) -> replacement
                ));
            return mapper.writeValueAsString(snapshot);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to create contract snapshot: " + e.getMessage());
        }
    }

    @Transactional
    public void failNegotiation(Long negotiationId, String reason, User user) {
        Negotiation negotiation = negotiationRepository.findById(negotiationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Negotiation not found"));

        // 1. Provera prava
        if (!negotiation.getStartedBy().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        // 2. Provera da li je pregovor još uvek aktivan
        if (negotiation.getStatus() != NegotiationStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only active negotiations can be failed");
        }

        // 3. Ažuriranje statusa pregovora
        negotiation.setStatus(NegotiationStatus.FAILED);
        negotiation.setFailureReason(reason != null ? reason.trim() : "No reason provided");
        negotiationRepository.save(negotiation);

        // 4. Odmrzavanje ponude
        Offer offer = negotiation.getOffer();
        offer.setStatus(OfferStatus.PUBLISHED); // Vraćamo je u PUBLISHED da bi bila dostupna za nove pregovore
        offer.setFrozenAt(null);
        offerRepository.save(offer);

        // 5. Zatvaranje trenutnog stanja u istoriji
        NegotiationStateHistory currentHistory = historyRepository
                .findByNegotiation_IdAndExitTimeIsNull(negotiation.getId())
                .orElse(null);
        
        if (currentHistory != null) {
            currentHistory.setExitTime(LocalDateTime.now());
            historyRepository.save(currentHistory);
        }
    }

    @Transactional(readOnly = true)
    public Page<Negotiation> getNegotiations(
            NegotiationStatus status, Long performerId, Long offerId, Long stateId, Pageable pageable, User user) {
        
        requireNegotiationManager(user.getId());

        return negotiationRepository.findAll(
            NegotiationSpecification.hasStatus(status)
            .and(NegotiationSpecification.hasPerformerId(performerId))
            .and(NegotiationSpecification.hasOfferId(offerId))
            .and(NegotiationSpecification.hasCurrentStateId(stateId)), 
            pageable
        );
    }
}
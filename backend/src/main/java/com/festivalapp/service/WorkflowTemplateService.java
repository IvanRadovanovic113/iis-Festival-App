package com.festivalapp.service;

import com.festivalapp.dto.*;
import com.festivalapp.model.*;
import com.festivalapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class WorkflowTemplateService {

    private final WorkflowTemplateRepository templateRepository;
    private final WorkflowStateRepository stateRepository;
    private final WorkflowTransitionRepository transitionRepository;
    private final TransitionConditionRepository conditionRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;

    private void requireNegotiationManager(Long userId) {
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Festival assignment is required"));
        
        if (assignment.getRole() != Role.NEGOTIATION_MANAGER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only negotiation managers can manage workflow templates");
        }
    }

    @Transactional
    public WorkflowTemplateDetailResponse createTemplate(WorkflowTemplateRequest request, User user) {
        requireNegotiationManager(user.getId());

        // Validacija: Bar jedno početno i bar jedno krajnje stanje
        boolean hasInitial = request.getStates().stream().anyMatch(WorkflowStateRequest::isInitial);
        boolean hasFinal = request.getStates().stream().anyMatch(WorkflowStateRequest::isFinalState);
        if (!hasInitial || !hasFinal) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Template must have at least one initial and one final state.");
        }

        // Validacija: Bar jedna dozvoljena tranzicija u sistemu
        if (request.getTransitions() == null || request.getTransitions().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Template must have at least one allowed transition.");
        }

        // 1. Čuvanje šablona
        WorkflowTemplate template = WorkflowTemplate.builder()
                .name(request.getName().trim())
                .archived(false)
                .build();
        WorkflowTemplate savedTemplate = templateRepository.save(template);

        // 2. Čuvanje stanja i mapiranje po imenu (privremeno)
        Map<String, WorkflowState> stateMap = new HashMap<>();
        for (WorkflowStateRequest stateReq : request.getStates()) {
            WorkflowState state = WorkflowState.builder()
                    .name(stateReq.getName().trim())
                    .initial(stateReq.isInitial())
                    .finalState(stateReq.isFinalState())
                    .defaultDeadlineDays(stateReq.getDefaultDeadlineDays())
                    .template(savedTemplate)
                    .build();
            WorkflowState savedState = stateRepository.save(state);
            stateMap.put(savedState.getName(), savedState);
            savedTemplate.getStates().add(savedState);
        }

        // 3. Sklapanje tranzicija
        List<WorkflowTransition> savedTransitions = new ArrayList<>();
        for (WorkflowTransitionRequest transReq : request.getTransitions()) {
            WorkflowState source = stateMap.get(transReq.getSourceStateName().trim());
            WorkflowState target = stateMap.get(transReq.getTargetStateName().trim());

            if (source == null || target == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid source or target state name in transition.");
            }

            List<TransitionCondition> conditions = new ArrayList<>();
            if (transReq.getConditionIds() != null && !transReq.getConditionIds().isEmpty()) {
                conditions = conditionRepository.findAllById(transReq.getConditionIds());
            }

            WorkflowTransition transition = WorkflowTransition.builder()
                    .label(transReq.getLabel().trim())
                    .sourceState(source)
                    .targetState(target)
                    .conditions(conditions)
                    .build();

            savedTransitions.add(transitionRepository.save(transition));
        }

        return WorkflowTemplateDetailResponse.from(savedTemplate, savedTransitions);
    }

    @Transactional(readOnly = true)
    public Page<WorkflowTemplateResponse> getTemplates(boolean archived, String searchTerm, Pageable pageable, User user) {
        requireNegotiationManager(user.getId());
        String search = (searchTerm != null && !searchTerm.trim().isEmpty()) ? searchTerm.trim() : null;

        if (search != null) {
            return templateRepository.findByArchivedAndNameContainingIgnoreCase(archived, search, pageable)
                    .map(WorkflowTemplateResponse::from);
        } else {
            return templateRepository.findByArchived(archived, pageable)
                    .map(WorkflowTemplateResponse::from);
        }
    }

    @Transactional(readOnly = true)
    public WorkflowTemplateDetailResponse getTemplateById(Long id, User user) {
        requireNegotiationManager(user.getId());

        WorkflowTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow template not found."));

        List<WorkflowTransition> transitions = transitionRepository.findAllTransitionsByTemplateId(id);

        return WorkflowTemplateDetailResponse.from(template, transitions);
    }

    @Transactional
    public WorkflowTemplateDetailResponse createNewVersion(Long originalId, User user) {
        requireNegotiationManager(user.getId());

        WorkflowTemplate original = templateRepository.findById(originalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Original template not found."));

        // Kopiranje korena preko self-reference (Ad-hoc)
        WorkflowTemplate newVersion = WorkflowTemplate.builder()
                .name(original.getName() + " (Copy)")
                .archived(false)
                .copiedFrom(original)
                .build();
        WorkflowTemplate savedVersion = templateRepository.save(newVersion);

        // Duboko kopiranje čvorova (Stanja)
        List<WorkflowState> originalStates = stateRepository.findByTemplateId(originalId);
        Map<Long, WorkflowState> oldIdToNewStateMap = new HashMap<>();

        for (WorkflowState oldState : originalStates) {
            WorkflowState newState = WorkflowState.builder()
                    .name(oldState.getName())
                    .initial(oldState.isInitial())
                    .finalState(oldState.isFinalState())
                    .defaultDeadlineDays(oldState.getDefaultDeadlineDays())
                    .template(savedVersion)
                    .build();
            WorkflowState savedState = stateRepository.save(newState);
            oldIdToNewStateMap.put(oldState.getId(), savedState);
            savedVersion.getStates().add(savedState);
        }

        // Duboko kopiranje grana (Tranzicija)
        List<WorkflowTransition> originalTransitions = transitionRepository.findAllTransitionsByTemplateId(originalId);
        List<WorkflowTransition> newTransitions = new ArrayList<>();

        for (WorkflowTransition oldTrans : originalTransitions) {
            WorkflowState newSource = oldIdToNewStateMap.get(oldTrans.getSourceState().getId());
            WorkflowState newTarget = oldIdToNewStateMap.get(oldTrans.getTargetState().getId());

            WorkflowTransition newTrans = WorkflowTransition.builder()
                    .label(oldTrans.getLabel())
                    .sourceState(newSource)
                    .targetState(newTarget)
                    .conditions(new ArrayList<>(oldTrans.getConditions())) // katalog veze ostaju iste
                    .build();

            newTransitions.add(transitionRepository.save(newTrans));
        }

        return WorkflowTemplateDetailResponse.from(savedVersion, newTransitions);
    }

    @Transactional
    public WorkflowTemplateResponse archiveTemplate(Long id, User user) {
        requireNegotiationManager(user.getId());

        WorkflowTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow template not found."));

        // TODO: Provera aktivnih pregovora kada se uvede entitet Negotiation
        boolean hasActiveNegotiations = false; 

        if (hasActiveNegotiations) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Archiving blocked. There are active negotiations using this template.");
        }

        template.setArchived(true);
        return WorkflowTemplateResponse.from(templateRepository.save(template));
    }

    @Transactional(readOnly = true)
        public List<WorkflowTemplateResponse> findAllActive() {
        return templateRepository.findByArchivedFalse().stream()
            .map(WorkflowTemplateResponse::from)
            .toList();
    }
}
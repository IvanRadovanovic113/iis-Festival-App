package com.festivalapp.service;

import com.festivalapp.dto.TransitionConditionRequest;
import com.festivalapp.dto.TransitionConditionResponse;
import com.festivalapp.model.Role;
import com.festivalapp.model.TransitionCondition;
import com.festivalapp.model.User;
import com.festivalapp.model.UserFestivalAssignment;
import com.festivalapp.repository.TransitionConditionRepository;
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
public class ConditionCatalogService {

    private final TransitionConditionRepository conditionRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;

    private void requireNegotiationManager(Long userId) {
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Festival assignment is required"));
        
        if (assignment.getRole() != Role.NEGOTIATION_MANAGER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only negotiation managers can manage the condition catalog");
        }
    }

    @Transactional
    public TransitionConditionResponse createCatalogCondition(TransitionConditionRequest request, User user) {
        requireNegotiationManager(user.getId());

        if (conditionRepository.existsByConditionKey(request.getConditionKey().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Condition key already exists in catalog.");
        }

        TransitionCondition condition = TransitionCondition.builder()
                .conditionKey(request.getConditionKey().trim())
                .label(request.getLabel().trim())
                .dataType(request.getDataType())
                .necessary(request.isNecessary())
                .build();

        return TransitionConditionResponse.from(conditionRepository.save(condition));
    }

    @Transactional(readOnly = true)
    public Page<TransitionConditionResponse> getCatalogConditions(String searchTerm, Pageable pageable, User user) {
        requireNegotiationManager(user.getId());
        
        Page<TransitionCondition> page;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            page = conditionRepository.findByLabelContainingIgnoreCase(searchTerm.trim(), pageable);
        } else {
            page = conditionRepository.findAll(pageable);
        }
        return page.map(TransitionConditionResponse::from);
    }
}
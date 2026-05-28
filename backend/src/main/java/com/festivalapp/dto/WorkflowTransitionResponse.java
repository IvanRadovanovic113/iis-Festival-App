package com.festivalapp.dto;

import com.festivalapp.model.WorkflowTransition;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class WorkflowTransitionResponse {
    private Long id;
    private String label;
    private Long sourceStateId;
    private Long targetStateId;
    private List<TransitionConditionResponse> conditions;

    public static WorkflowTransitionResponse from(WorkflowTransition transition) {
        WorkflowTransitionResponse response = new WorkflowTransitionResponse();
        response.id = transition.getId();
        response.label = transition.getLabel();
        response.sourceStateId = transition.getSourceState().getId();
        response.targetStateId = transition.getTargetState().getId();
        response.conditions = transition.getConditions().stream()
                .map(TransitionConditionResponse::from)
                .collect(Collectors.toList());
        return response;
    }
}
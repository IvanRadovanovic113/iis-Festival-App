package com.festivalapp.dto;

import com.festivalapp.model.WorkflowState;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkflowStateResponse {
    private Long id;
    private String name;
    private boolean initial;
    private boolean finalState;
    private Integer defaultDeadlineDays;
    private int currentNegotiationsCount; // Za grafički prikaz (trenutno 0)

    public static WorkflowStateResponse from(WorkflowState state) {
        WorkflowStateResponse response = new WorkflowStateResponse();
        response.id = state.getId();
        response.name = state.getName();
        response.initial = state.isInitial();
        response.finalState = state.isFinalState();
        response.defaultDeadlineDays = state.getDefaultDeadlineDays();
        response.currentNegotiationsCount = 0; // Privremeno fiksirano na 0
        return response;
    }
}
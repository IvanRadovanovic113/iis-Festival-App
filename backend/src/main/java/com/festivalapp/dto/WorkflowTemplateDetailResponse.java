package com.festivalapp.dto;

import com.festivalapp.model.WorkflowTemplate;
import com.festivalapp.model.WorkflowTransition;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class WorkflowTemplateDetailResponse extends WorkflowTemplateResponse {
    private List<WorkflowStateResponse> states;
    private List<WorkflowTransitionResponse> transitions;

    public static WorkflowTemplateDetailResponse from(WorkflowTemplate template, List<WorkflowTransition> transitions) {
        WorkflowTemplateDetailResponse response = new WorkflowTemplateDetailResponse();
        
        // Mapiranje osnovnih polja iz roditeljske from metode
        WorkflowTemplateResponse base = WorkflowTemplateResponse.from(template);
        response.setId(base.getId());
        response.setName(base.getName());
        response.setArchived(base.isArchived());
        response.setCreatedAt(base.getCreatedAt());
        response.setCopiedFromId(base.getCopiedFromId());
        response.setStatesCount(base.getStatesCount());
        response.setActiveNegotiationsCount(base.getActiveNegotiationsCount());

        // Mapiranje specifičnih detalja grafa
        response.states = template.getStates().stream()
                .map(WorkflowStateResponse::from)
                .collect(Collectors.toList());
                
        response.transitions = transitions.stream()
                .map(WorkflowTransitionResponse::from)
                .collect(Collectors.toList());
                
        return response;
    }
}
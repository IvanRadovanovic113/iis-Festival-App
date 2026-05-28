package com.festivalapp.dto;

import com.festivalapp.model.WorkflowTemplate;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class WorkflowTemplateResponse {
    private Long id;
    private String name;
    private boolean archived;
    private LocalDateTime createdAt;
    private Long copiedFromId;
    private int statesCount;
    private int activeNegotiationsCount; // Rezervisano za sledeći sprint (Jira zahtev)

    public static WorkflowTemplateResponse from(WorkflowTemplate template) {
        WorkflowTemplateResponse response = new WorkflowTemplateResponse();
        response.id = template.getId();
        response.name = template.getName();
        response.archived = template.isArchived();
        response.createdAt = template.getCreatedAt();
        response.copiedFromId = template.getCopiedFrom() != null ? template.getCopiedFrom().getId() : null;
        response.statesCount = template.getStates() != null ? template.getStates().size() : 0;
        response.activeNegotiationsCount = 0; // Privremeno fiksirano na 0
        return response;
    }
}
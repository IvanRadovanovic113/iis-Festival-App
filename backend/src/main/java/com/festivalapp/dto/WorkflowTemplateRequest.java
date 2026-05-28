package com.festivalapp.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class WorkflowTemplateRequest {

    @NotBlank(message = "Template name is required.")
    @Size(max = 255)
    private String name;

    @NotEmpty(message = "Workflow template must have at least one state.")
    private List<WorkflowStateRequest> states;

    private List<WorkflowTransitionRequest> transitions;
}
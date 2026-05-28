package com.festivalapp.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkflowStateRequest {

    @NotBlank(message = "State name is required.")
    private String name;

    private boolean initial;

    private boolean finalState;

    @NotNull(message = "Default deadline days field is required.")
    @Min(value = 0, message = "Deadline days cannot be negative.")
    private Integer defaultDeadlineDays;
}
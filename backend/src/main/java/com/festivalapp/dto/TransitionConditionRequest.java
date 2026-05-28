package com.festivalapp.dto;

import com.festivalapp.model.ConditionDataType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransitionConditionRequest {

    @NotBlank(message = "Condition key is required.")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Key must be uppercase alphanumeric with underscores.")
    private String conditionKey;

    @NotBlank(message = "Label is required.")
    @Size(max = 255)
    private String label;

    @NotNull(message = "Data type is required.")
    private ConditionDataType dataType;

    private boolean necessary;
}
package com.festivalapp.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class TransitionRequest {

    @NotNull(message = "Transition ID is required.")
    private Long transitionId;

    @NotEmpty(message = "Condition values list cannot be empty.")
    private List<ConditionValueDto> conditionValues;
}
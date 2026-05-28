package com.festivalapp.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class WorkflowTransitionRequest {

    @NotBlank(message = "Transition label is required.")
    private String label;

    @NotBlank(message = "Source state name is required.")
    private String sourceStateName;

    @NotBlank(message = "Target state name is required.")
    private String targetStateName;

    private List<Long> conditionIds; // ID-evi uslova iz kataloga koji se "lepe" na tranziciju
}
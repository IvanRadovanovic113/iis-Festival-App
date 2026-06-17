package com.festivalapp.dto;

import com.festivalapp.model.NegotiationStatus;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class TransitionDto {
    private Long id;
    private String label;
    private List<TransitionConditionResponse> requiredConditions;
}
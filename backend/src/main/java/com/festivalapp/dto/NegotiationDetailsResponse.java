package com.festivalapp.dto;

import com.festivalapp.model.NegotiationStatus;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.time.LocalDateTime;

@Data
@Builder
public class NegotiationDetailsResponse {
    private Long id;
    private String performerName;
    private String status;
    private String currentState;
    private List<NegotiationStateHistoryDto> history;
    private List<ConditionValueDto> enteredConditions;
    private List<TransitionDto> availableTransitions;
    private String offerTitle;
    private LocalDateTime lastUpdated;
}
package com.festivalapp.dto;

import com.festivalapp.dto.*;
import com.festivalapp.model.*;
import java.util.stream.Collectors;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Comparator;

public class NegotiationMapper {

    public static NegotiationResponse toResponse(Negotiation n) {
    return NegotiationResponse.builder()
        .id(n.getId())
        .performerName(n.getPerformer().getStageName())
        .offerTitle(n.getOffer().getLocation())
        .currentStateName(n.getCurrentState().getName())
        .status(n.getStatus())
        .build();
    }

    public static NegotiationDetailsResponse toDetailsResponse(Negotiation n, List<NegotiationStateHistory> history, List<NegotiationConditionValue> conditions, List<TransitionDto> transitions) {

        LocalDateTime lastUpdated = history.stream()
                .map(NegotiationStateHistory::getEntryTime)
                .max(Comparator.naturalOrder())
                .orElse(LocalDateTime.now());

        return NegotiationDetailsResponse.builder()
                .id(n.getId())
                .offerTitle(n.getOffer().getLocation())
                .lastUpdated(lastUpdated)
                .status(n.getStatus().toString())
                .currentState(n.getCurrentState().getName())
                .history(history.stream().map(h -> {
                    NegotiationStateHistoryDto dto = new NegotiationStateHistoryDto();
                    dto.setStateName(h.getState().getName());
                    dto.setEntryTime(h.getEntryTime());
                    dto.setExitTime(h.getExitTime());
                    return dto;
                }).collect(Collectors.toList()))
                .enteredConditions(conditions.stream().map(c -> new ConditionValueDto(c.getCondition().getId(), c.getValue(),c.getCondition().getLabel())).collect(Collectors.toList()))
                .availableTransitions(transitions)
                .build();
    }
}
package com.festivalapp.dto;

import com.festivalapp.dto.*;
import com.festivalapp.model.*;
import java.util.stream.Collectors;
import java.util.List;

public class NegotiationMapper {

    public static NegotiationResponse toResponse(Negotiation n) {
    return NegotiationResponse.builder()
        .id(n.getId())
        .performerName(n.getPerformer().getStageName())
        .offerTitle(n.getOffer().getLocation())
        .currentStateName(n.getCurrentState().getName())
        .status(n.getStatus())
        // .startedAt(n.getStartedAt())
        .build();
    }

    public static NegotiationDetailsResponse toDetailsResponse(Negotiation n, List<NegotiationStateHistory> history, List<NegotiationConditionValue> conditions) {
        return NegotiationDetailsResponse.builder()
            .id(n.getId())
            .performerName(n.getPerformer().getStageName())
            .status(n.getStatus().toString())
            .currentState(n.getCurrentState().getName())
            .history(history.stream().map(h -> {
                NegotiationStateHistoryDto dto = new NegotiationStateHistoryDto();
                dto.setStateName(h.getState().getName());
                dto.setEntryTime(h.getEntryTime());
                dto.setExitTime(h.getExitTime());
                return dto;
            }).collect(Collectors.toList()))
            .enteredConditions(conditions.stream().map(c -> new ConditionValueDto(c.getCondition().getId(), c.getValue())).collect(Collectors.toList()))
            .build();
    }
}
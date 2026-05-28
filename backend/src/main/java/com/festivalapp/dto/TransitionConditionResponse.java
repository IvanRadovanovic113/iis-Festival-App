package com.festivalapp.dto;

import com.festivalapp.model.TransitionCondition;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransitionConditionResponse {
    private Long id;
    private String conditionKey;
    private String label;
    private String dataType;
    private boolean necessary;

    public static TransitionConditionResponse from(TransitionCondition condition) {
        TransitionConditionResponse response = new TransitionConditionResponse();
        response.id = condition.getId();
        response.conditionKey = condition.getConditionKey();
        response.label = condition.getLabel();
        response.dataType = condition.getDataType().name();
        response.necessary = condition.isNecessary();
        return response;
    }
}
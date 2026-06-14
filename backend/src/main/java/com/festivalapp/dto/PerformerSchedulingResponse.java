package com.festivalapp.dto;

import com.festivalapp.model.Contract;
import com.festivalapp.model.PerformerSchedulingStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PerformerSchedulingResponse {

    private Long contractId;
    private Long negotiationId;
    private String performerName;
    private Long stageId;
    private String stageName;
    private PerformerSchedulingStatus schedulingStatus;

    public static PerformerSchedulingResponse from(Contract contract) {
        var negotiation = contract.getNegotiation();
        var performer = negotiation.getPerformer();
        var stage = contract.getStage();

        return PerformerSchedulingResponse.builder()
            .contractId(contract.getId())
            .negotiationId(negotiation.getId())
            .performerName(performer.getStageName())
            .stageId(stage != null ? stage.getStageId() : null)
            .stageName(stage != null ? stage.getName() : null)
            .schedulingStatus(contract.getSchedulingStatus())
            .build();
    }
}

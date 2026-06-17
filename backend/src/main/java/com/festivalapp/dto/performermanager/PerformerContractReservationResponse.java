package com.festivalapp.dto.performermanager;

import com.festivalapp.dto.eventorganization.StageResourceResponse;
import com.festivalapp.model.Contract;
import com.festivalapp.model.PerformerSchedulingStatus;
import com.festivalapp.model.eventorganization.EventReservationRequest;
import com.festivalapp.model.eventorganization.EventReservationStatus;
import com.festivalapp.model.eventorganization.StageResource;

import java.time.LocalDate;
import java.util.List;

public record PerformerContractReservationResponse(
    Long contractId,
    Long negotiationId,
    String performerName,
    Long stageId,
    String stageName,
    LocalDate performanceDate,
    Integer durationMinutes,
    PerformerSchedulingStatus schedulingStatus,
    Long reservationRequestId,
    EventReservationStatus reservationStatus,
    List<StageResourceResponse> stageResources
) {
    public static PerformerContractReservationResponse from(
            Contract contract,
            EventReservationRequest reservationRequest,
            List<StageResource> stageResources) {
        var negotiation = contract.getNegotiation();
        var offer = negotiation.getOffer();
        var performer = negotiation.getPerformer();
        var stage = contract.getStage();

        return new PerformerContractReservationResponse(
            contract.getId(),
            negotiation.getId(),
            performer.getStageName(),
            stage.getStageId(),
            stage.getName(),
            offer.getPerformanceDate().toLocalDate(),
            offer.getDurationMinutes(),
            contract.getSchedulingStatus(),
            reservationRequest == null ? null : reservationRequest.getId(),
            reservationRequest == null ? null : reservationRequest.getStatus(),
            stageResources.stream().map(StageResourceResponse::from).toList()
        );
    }
}

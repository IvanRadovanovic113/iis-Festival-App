package com.festivalapp.dto;

import com.festivalapp.model.Contract;

public class ContractMapper {

    public static ContractResponse toResponse(Contract contract) {
        return ContractResponse.builder()
                .id(contract.getId())
                .negotiationId(contract.getNegotiation().getId())
                .offerTitle(contract.getNegotiation().getOffer().getLocation())
                .performerName(contract.getNegotiation().getPerformer().getStageName())
                .signedByUserName(contract.getSignedBy().getUsername())
                .signedAt(contract.getSignedAt())
                .conditionSnapshotJson(contract.getConditionSnapshotJson())
                .build();
    }
}
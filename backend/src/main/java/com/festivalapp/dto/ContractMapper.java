package com.festivalapp.dto;

import com.festivalapp.model.Contract;

public class ContractMapper {

    public static ContractResponse toResponse(Contract contract) {
        var n = contract.getNegotiation();
        
        return ContractResponse.builder()
                .id(contract.getId())
                .negotiationId(n != null ? n.getId() : null)
                .offerTitle((n != null && n.getOffer() != null) ? n.getOffer().getLocation() : "N/A")
                .performerName((n != null && n.getPerformer() != null) ? n.getPerformer().getStageName() : "N/A")
                .signedByUserName(contract.getSignedBy() != null ? contract.getSignedBy().getUsername() : "System")
                .signedAt(contract.getSignedAt())
                .conditionSnapshotJson(contract.getConditionSnapshotJson())
                .build();
    }
}
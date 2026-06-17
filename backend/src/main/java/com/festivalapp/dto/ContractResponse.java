package com.festivalapp.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ContractResponse {
    private Long id;
    private Long negotiationId;
    private String offerTitle;
    private String performerName;
    private String signedByUserName;
    private LocalDateTime signedAt;
    private String conditionSnapshotJson;
}
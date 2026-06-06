package com.festivalapp.dto;

import com.festivalapp.model.NegotiationStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class NegotiationResponse {
    private Long id;
    private String performerName;
    private String offerTitle; 
    private String currentStateName;
    private NegotiationStatus status;
    private LocalDateTime startedAt;
}
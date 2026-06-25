package com.festivalapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PerformerStatsDto {
    private String stageName;
    private Long totalNegotiations;
    private Long successfulNegotiations;
    private Long failedNegotiations;
    private Double successRate;
}
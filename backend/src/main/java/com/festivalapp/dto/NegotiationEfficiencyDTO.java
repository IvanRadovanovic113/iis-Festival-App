package com.festivalapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NegotiationEfficiencyDTO {
    private Long totalCount;
    private Long successfulCount;
    private Double successPercentage;
}
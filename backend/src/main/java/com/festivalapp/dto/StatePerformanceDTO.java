package com.festivalapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatePerformanceDTO {
    private String stateName;
    private String templateName;
    private Double averageDurationHours;
    private Long count;
}
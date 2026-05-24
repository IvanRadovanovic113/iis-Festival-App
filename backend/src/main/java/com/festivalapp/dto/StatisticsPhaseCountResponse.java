package com.festivalapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StatisticsPhaseCountResponse {

    private Long phaseId;
    private String name;
    private Integer orderIndex;
    private long count;
}

package com.festivalapp.dto.eventorganization.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResourceStageOccupancyResponse {

    private long stageId;
    private String stageName;
    private long totalReservations;
    private long approvedReservations;
    private double occupancyPercent;
}

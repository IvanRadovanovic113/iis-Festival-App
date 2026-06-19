package com.festivalapp.dto.eventorganization;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ResourceAnalyticsResponse {

    private long totalReservations;
    private String mostUsedResourceName;
    private long mostUsedResourceCount;
    private double avgStageOccupancy;
    private long extraResourceRequests;

    private List<ResourceTopResourceResponse> topResources;
    private List<ResourceStageOccupancyResponse> stageOccupancies;
}

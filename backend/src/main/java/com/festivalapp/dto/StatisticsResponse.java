package com.festivalapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class StatisticsResponse {

    private List<StatisticsCampaignOptionResponse> campaigns;
    private List<StatisticsAdTypeOptionResponse> adTypes;
    private long totalAds;
    private List<StatisticsPhaseCountResponse> phaseCounts;
    private List<StatisticsTypeCountResponse> typeCounts;
}

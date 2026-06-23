package com.festivalapp.prodaja.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ActionsAnalyticsResponse {
    private List<BundleDealStatsDto> bundleDeals;
    private List<DailyBundleImpactDto> dailyImpact;
}

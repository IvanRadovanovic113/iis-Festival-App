package com.festivalapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CampaignStatsResponse {

    private long totalAds;
    private long todo;
    private long draft;
    private long approvedTechnical;
    private long approved;
    private long rejected;
}

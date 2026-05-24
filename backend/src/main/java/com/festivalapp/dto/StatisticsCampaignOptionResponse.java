package com.festivalapp.dto;

import com.festivalapp.model.Campaign;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StatisticsCampaignOptionResponse {

    private Long campaignId;
    private String campaignName;
    private String festivalName;

    public static StatisticsCampaignOptionResponse from(Campaign campaign) {
        return new StatisticsCampaignOptionResponse(
            campaign.getCampaignId(),
            campaign.getName(),
            campaign.getFestival().getName()
        );
    }
}

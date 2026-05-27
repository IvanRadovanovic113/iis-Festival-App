package com.festivalapp.dto;

import com.festivalapp.model.Campaign;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreativeCampaignResponse {

    private Long campaignId;
    private String campaignName;
    private String festivalName;
    private String festivalLocation;
    private String festivalStatus;
    private String startDate;
    private String endDate;
    private long eligibleAds;

    public static CreativeCampaignResponse from(Campaign campaign, long eligibleAds) {
        return new CreativeCampaignResponse(
            campaign.getCampaignId(),
            campaign.getName(),
            campaign.getFestival().getName(),
            campaign.getFestival().getLocation(),
            campaign.getFestival().getStatus().name(),
            campaign.getStartDate().toString(),
            campaign.getEndDate().toString(),
            eligibleAds
        );
    }
}

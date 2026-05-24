package com.festivalapp.dto;

import com.festivalapp.model.Campaign;
import com.festivalapp.model.Festival;
import com.festivalapp.model.FestivalStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class FestivalCampaignOverviewResponse {

    private Long festivalId;
    private String name;
    private String location;
    private FestivalStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean hasCampaign;
    private Long campaignId;
    private String campaignName;
    private String managerName;

    public static FestivalCampaignOverviewResponse from(Festival festival, Campaign campaign) {
        FestivalCampaignOverviewResponse response = new FestivalCampaignOverviewResponse();
        response.festivalId = festival.getFestivalId();
        response.name = festival.getName();
        response.location = festival.getLocation();
        response.status = festival.getStatus();
        response.startDate = festival.getStartDate();
        response.endDate = festival.getEndDate();
        response.hasCampaign = campaign != null;
        response.campaignId = campaign != null ? campaign.getCampaignId() : null;
        response.campaignName = campaign != null ? campaign.getName() : null;
        response.managerName = campaign != null ? campaign.getManagerUser().getUsername() : null;
        return response;
    }
}

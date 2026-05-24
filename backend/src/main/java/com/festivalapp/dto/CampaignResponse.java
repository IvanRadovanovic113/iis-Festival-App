package com.festivalapp.dto;

import com.festivalapp.model.Campaign;
import com.festivalapp.model.FestivalStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CampaignResponse {

    private Long campaignId;
    private Long festivalId;
    private String festivalName;
    private String festivalLocation;
    private FestivalStatus festivalStatus;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long managerUserId;
    private String managerName;

    public static CampaignResponse from(Campaign campaign) {
        CampaignResponse response = new CampaignResponse();
        response.campaignId = campaign.getCampaignId();
        response.festivalId = campaign.getFestival().getFestivalId();
        response.festivalName = campaign.getFestival().getName();
        response.festivalLocation = campaign.getFestival().getLocation();
        response.festivalStatus = campaign.getFestival().getStatus();
        response.name = campaign.getName();
        response.description = campaign.getDescription();
        response.startDate = campaign.getStartDate();
        response.endDate = campaign.getEndDate();
        response.managerUserId = campaign.getManagerUser().getId();
        response.managerName = campaign.getManagerUser().getUsername();
        return response;
    }
}

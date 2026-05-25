package com.festivalapp.dto;

import com.festivalapp.model.Ad;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AdResponse {

    private Long adId;
    private String name;
    private String description;
    private String typeName;
    private Long adTypeId;
    private String contentType;
    private LocalDate lastChangeDate;
    private Integer versionNumber;
    private String status;
    private Long currentPhaseId;
    private String contentValue;
    private String festivalName;
    private String campaignName;

    public static AdResponse from(Ad ad) {
        AdResponse response = new AdResponse();
        response.adId = ad.getAdId();
        response.name = ad.getName();
        response.description = ad.getDescription();
        response.typeName = ad.getAdType().getName();
        response.adTypeId = ad.getAdType().getAdTypeId();
        response.contentType = ad.getAdType().getContentType();
        response.lastChangeDate = ad.getLastChangeDate();
        response.versionNumber = ad.getVersionNumber();
        response.status = ad.getCurrentPhase().getName();
        response.currentPhaseId = ad.getCurrentPhase().getPhaseId();
        response.contentValue = ad.getContentFileName();
        response.festivalName = ad.getCampaign().getFestival().getName();
        response.campaignName = ad.getCampaign().getName();
        return response;
    }
}

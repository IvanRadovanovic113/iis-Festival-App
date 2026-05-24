package com.festivalapp.dto;

import com.festivalapp.model.Ad;
import com.festivalapp.model.AdVersion;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AdVersionDetailResponse {

    private Long adId;
    private String name;
    private String description;
    private String typeName;
    private String contentType;
    private String contentValue;
    private String festivalName;
    private String campaignName;
    private String status;
    private Integer versionNumber;
    private LocalDate changedAt;
    private boolean current;

    public static AdVersionDetailResponse from(Ad ad, AdVersion version, boolean current) {
        AdVersionDetailResponse response = new AdVersionDetailResponse();
        response.adId = ad.getAdId();
        response.name = version.getName();
        response.description = version.getDescription();
        response.typeName = ad.getAdType().getName();
        response.contentType = ad.getAdType().getContentType();
        response.contentValue = version.getContentValue();
        response.festivalName = ad.getCampaign().getFestival().getName();
        response.campaignName = ad.getCampaign().getName();
        response.status = version.getPhaseName();
        response.versionNumber = version.getVersionNumber();
        response.changedAt = version.getChangedAt();
        response.current = current;
        return response;
    }
}

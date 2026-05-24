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
    private LocalDate lastChangeDate;
    private Integer versionNumber;
    private String status;
    private String contentFileName;

    public static AdResponse from(Ad ad) {
        AdResponse response = new AdResponse();
        response.adId = ad.getAdId();
        response.name = ad.getName();
        response.description = ad.getDescription();
        response.typeName = ad.getAdType().getName();
        response.adTypeId = ad.getAdType().getAdTypeId();
        response.lastChangeDate = ad.getLastChangeDate();
        response.versionNumber = ad.getVersionNumber();
        response.status = ad.getCurrentPhase().getName();
        response.contentFileName = ad.getContentFileName();
        return response;
    }
}

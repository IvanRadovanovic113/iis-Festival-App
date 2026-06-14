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
    private String currentPhaseAssignedRole;
    private String contentValue;
    private String contentUrl;
    private String contentOriginalFileName;
    private String contentMimeType;
    private Long contentSize;
    private String festivalName;
    private String festivalLocation;
    private String campaignName;
    private String rejectionReason;
    private AdPromotionResponse promotion;

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
        response.currentPhaseAssignedRole = ad.getCurrentPhase().getAssignedRole().name();
        response.contentValue = resolveContentValue(ad);
        response.contentUrl = ad.getContentStoragePath() != null ? "/api/ad-content/ads/" + ad.getAdId() : null;
        response.contentOriginalFileName = ad.getContentOriginalFileName();
        response.contentMimeType = ad.getContentMimeType();
        response.contentSize = ad.getContentSize();
        response.festivalName = ad.getCampaign().getFestival().getName();
        response.festivalLocation = ad.getCampaign().getFestival().getLocation();
        response.campaignName = ad.getCampaign().getName();
        response.rejectionReason = ad.getRejectionReason();
        response.promotion = ad.getPromotion() != null ? AdPromotionResponse.from(ad.getPromotion()) : null;
        return response;
    }

    private static String resolveContentValue(Ad ad) {
        String contentType = ad.getAdType().getContentType();
        if ("Image".equalsIgnoreCase(contentType) || "Audio".equalsIgnoreCase(contentType) || "Video".equalsIgnoreCase(contentType)) {
            return ad.getContentStoragePath() == null ? ad.getContentFileName() : null;
        }
        return ad.getContentText() != null ? ad.getContentText() : ad.getContentFileName();
    }
}

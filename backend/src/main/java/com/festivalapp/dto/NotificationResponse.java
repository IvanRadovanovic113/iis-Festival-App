package com.festivalapp.dto;

import com.festivalapp.model.AdNotification;
import com.festivalapp.model.NotificationType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationResponse {
    private Long notificationId;
    private Long adId;
    private Long festivalId;
    private Long campaignId;
    private String adName;
    private String campaignName;
    private String festivalName;
    private NotificationType type;
    private String title;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;

    public static NotificationResponse from(AdNotification notification) {
        NotificationResponse response = new NotificationResponse();
        response.notificationId = notification.getNotificationId();
        response.adId = notification.getAd().getAdId();
        response.festivalId = notification.getAd().getCampaign().getFestival().getFestivalId();
        response.campaignId = notification.getAd().getCampaign().getCampaignId();
        response.adName = notification.getAd().getName();
        response.campaignName = notification.getAd().getCampaign().getName();
        response.festivalName = notification.getAd().getCampaign().getFestival().getName();
        response.type = notification.getType();
        response.title = notification.getTitle();
        response.message = notification.getMessage();
        response.read = notification.isRead();
        response.createdAt = notification.getCreatedAt();
        return response;
    }
}

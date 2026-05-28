package com.festivalapp.dto;

import com.festivalapp.model.AdPromotion;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class AdPromotionResponse {
    private Long promotionId;
    private String channel;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal pricePerDay;

    public static AdPromotionResponse from(AdPromotion promotion) {
        AdPromotionResponse response = new AdPromotionResponse();
        response.promotionId = promotion.getPromotionId();
        response.channel = promotion.getChannel().name();
        response.startDate = promotion.getStartDate();
        response.endDate = promotion.getEndDate();
        response.pricePerDay = promotion.getPricePerDay();
        return response;
    }
}

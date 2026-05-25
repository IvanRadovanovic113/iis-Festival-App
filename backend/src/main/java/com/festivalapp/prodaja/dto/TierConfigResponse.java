package com.festivalapp.prodaja.dto;

import com.festivalapp.prodaja.model.TierConfig;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TierConfigResponse {

    private String tier;
    private Integer minTickets;
    private Integer discountPercent;

    public static TierConfigResponse from(TierConfig c) {
        return TierConfigResponse.builder()
            .tier(c.getTier().name())
            .minTickets(c.getMinTickets())
            .discountPercent(c.getDiscountPercent())
            .build();
    }
}

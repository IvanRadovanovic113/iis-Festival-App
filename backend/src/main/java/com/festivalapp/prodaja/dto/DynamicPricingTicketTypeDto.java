package com.festivalapp.prodaja.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class DynamicPricingTicketTypeDto {
    private Long ticketTypeId;
    private String name;
    private Boolean dynamicPricingActive;
    private Integer totalTicketsSold;
    private BigDecimal baselineRevenue;
    private BigDecimal revenuePremium;
    private List<DailyPriceSalesDto> dailyData;
}

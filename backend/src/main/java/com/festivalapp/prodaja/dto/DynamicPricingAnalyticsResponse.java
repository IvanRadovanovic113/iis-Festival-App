package com.festivalapp.prodaja.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class DynamicPricingAnalyticsResponse {
    private BigDecimal totalRevenuePremium;
    private BigDecimal totalBaselineRevenue;
    private List<DynamicPricingTicketTypeDto> ticketTypes;
}

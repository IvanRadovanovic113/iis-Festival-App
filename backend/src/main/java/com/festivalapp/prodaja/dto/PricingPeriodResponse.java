package com.festivalapp.prodaja.dto;

import com.festivalapp.prodaja.model.PricingPeriod;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PricingPeriodResponse {

    private Long pricingPeriodId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal basePrice;
    private BigDecimal minPrice;
    private Boolean dynamicPricingActive;
    private Long ticketTypeId;

    public static PricingPeriodResponse from(PricingPeriod p) {
        PricingPeriodResponse r = new PricingPeriodResponse();
        r.pricingPeriodId = p.getPricingPeriodId();
        r.startDate = p.getStartDate();
        r.endDate = p.getEndDate();
        r.basePrice = p.getBasePrice();
        r.minPrice = p.getMinPrice();
        r.dynamicPricingActive = p.getDynamicPricingActive();
        r.ticketTypeId = p.getTicketType().getTicketTypeId();
        return r;
    }
}

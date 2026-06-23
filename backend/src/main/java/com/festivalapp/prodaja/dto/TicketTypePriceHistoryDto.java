package com.festivalapp.prodaja.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class TicketTypePriceHistoryDto {
    private String datum;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private boolean manual;
    private String reason;
}

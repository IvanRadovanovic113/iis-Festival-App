package com.festivalapp.prodaja.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class TicketTypeSummaryDto {
    private Long ticketTypeId;
    private String name;
    private Integer soldCount;
    private Integer totalQuantity;
    private BigDecimal currentPrice;
    private BigDecimal revenue;
    /** "WARNING" | "HIGHLIGHT" | "NORMAL" */
    private String performanceStatus;
}

package com.festivalapp.prodaja.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class TicketTypeSalesByPeriodDto {
    private Long periodId;
    private String startDate;
    private String endDate;
    private BigDecimal basePrice;
    private BigDecimal currentPrice;
    private Integer ticketsSold;
    private BigDecimal revenue;
}

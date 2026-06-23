package com.festivalapp.prodaja.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class TicketTypeSalesDataPointDto {
    private String date;
    private Integer ticketsSold;
    private BigDecimal revenue;
}

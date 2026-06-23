package com.festivalapp.prodaja.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class RevenueDataPointDto {
    private String date;
    private BigDecimal revenue;
    private Integer ticketsSold;
}

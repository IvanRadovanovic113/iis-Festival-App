package com.festivalapp.prodaja.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class BundleDealStatsDto {
    private Long akcijaId;
    private String ticketTypeName;
    private Integer kupiKarata;
    private Integer dobijaKarata;
    private LocalDate vaziOd;
    private LocalDate vaziDo;
    private Boolean active;
    private Integer applicationsCount;
    private Integer totalFreeTickets;
    private BigDecimal totalRevenue;
    private BigDecimal estimatedValueLost;
}

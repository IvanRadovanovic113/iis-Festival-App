package com.festivalapp.prodaja.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class PromoCodeStatsDto {
    private Long promoCodeId;
    private String code;
    private Integer discountPercent;
    private Boolean active;
    private LocalDate validFrom;
    private LocalDate validTo;
    private Integer usedCount;
    private Integer maxUses;
    private BigDecimal totalRevenue;
    private BigDecimal estimatedValueLost;
}

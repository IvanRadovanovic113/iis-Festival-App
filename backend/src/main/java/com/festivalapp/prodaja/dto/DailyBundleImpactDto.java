package com.festivalapp.prodaja.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DailyBundleImpactDto {
    private String date;
    private Integer purchasesCount;
    private BigDecimal revenue;
}

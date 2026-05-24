package com.festivalapp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PricingPeriodRequest {

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal basePrice;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal minPrice;

    @NotNull
    private Boolean dynamicPricingActive;
}

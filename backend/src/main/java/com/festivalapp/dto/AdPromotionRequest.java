package com.festivalapp.dto;

import com.festivalapp.model.PromotionChannel;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class AdPromotionRequest {

    @NotNull
    private PromotionChannel channel;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal pricePerDay;
}

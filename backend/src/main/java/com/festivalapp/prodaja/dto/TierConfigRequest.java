package com.festivalapp.prodaja.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TierConfigRequest {

    @NotNull
    @Min(1)
    private Integer minTickets;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer discountPercent;
}

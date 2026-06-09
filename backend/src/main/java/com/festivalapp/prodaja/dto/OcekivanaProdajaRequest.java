package com.festivalapp.prodaja.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OcekivanaProdajaRequest {

    @NotNull
    @Min(1)
    private Integer brojKarata;

    @NotNull
    @Min(1)
    private Integer brojSati;

    @NotNull
    @DecimalMin(value = "0.01")
    @DecimalMax(value = "1.00")
    private BigDecimal agresivnost;

    @NotNull
    @Min(1)
    private Integer intervalMinuti;

    @NotNull
    @Min(1)
    private Integer scarcityPragNizak;

    @NotNull
    @Min(1)
    private Integer scarcityPragVisok;

    @NotNull
    @DecimalMin(value = "1.00")
    private BigDecimal scarcityMultiplikatorNizak;

    @NotNull
    @DecimalMin(value = "1.00")
    private BigDecimal scarcityMultiplikatorVisok;
}

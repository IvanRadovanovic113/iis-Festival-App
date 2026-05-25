package com.festivalapp.prodaja.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BundleDealRequest {

    @NotNull
    private Long ticketTypeId;

    @NotNull
    @Min(1)
    private Integer kupiKarata;

    @NotNull
    @Min(1)
    private Integer dobijaKarata;

    @NotNull
    private LocalDate vaziOd;

    @NotNull
    private LocalDate vaziDo;

    @NotNull
    @Min(1)
    private Integer dostupnoAkcija;
}

package com.festivalapp.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PromoCodeRequest {

    @NotBlank
    private String code;

    @NotNull
    @Min(1)
    @Max(100)
    private Integer discountPercent;

    @NotNull
    private LocalDate validFrom;

    @NotNull
    private LocalDate validTo;

    /** null = unlimited */
    @Min(1)
    private Integer maxUses;
}

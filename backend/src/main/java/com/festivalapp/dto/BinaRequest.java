package com.festivalapp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BinaRequest {

    @NotBlank
    private String naziv;

    @NotNull
    @Min(1)
    private Integer kapacitet;

    @NotBlank
    private String lokacija;
}

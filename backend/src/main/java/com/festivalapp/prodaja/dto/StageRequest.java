package com.festivalapp.prodaja.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StageRequest {

    @NotBlank
    private String name;

    @NotNull
    @Min(1)
    private Integer capacity;

    @NotBlank
    private String location;
}

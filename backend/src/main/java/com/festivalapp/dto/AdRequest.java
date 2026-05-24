package com.festivalapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    private Long adTypeId;
}

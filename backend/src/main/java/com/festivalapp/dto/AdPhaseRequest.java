package com.festivalapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdPhaseRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    private Integer orderIndex;

    @NotNull
    private Boolean emailNotification;

    @NotNull
    private Long adTypeId;
}

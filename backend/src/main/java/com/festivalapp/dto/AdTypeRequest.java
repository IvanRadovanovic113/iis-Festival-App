package com.festivalapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdTypeRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotBlank
    private String contentType;

    @NotEmpty
    private List<Long> phaseIds;
}

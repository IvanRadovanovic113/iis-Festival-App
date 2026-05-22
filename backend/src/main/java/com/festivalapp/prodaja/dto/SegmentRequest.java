package com.festivalapp.prodaja.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SegmentRequest {

    @NotBlank
    private String name;
}

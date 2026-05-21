package com.festivalapp.prodaja.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BinaSegmentRequest {

    private Long segmentId;

    @NotNull
    @Min(1)
    private Integer kapacitet;
}

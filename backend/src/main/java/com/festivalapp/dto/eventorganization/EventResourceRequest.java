package com.festivalapp.dto.eventorganization;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventResourceRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String type;

    private String description;

    @NotNull
    @Min(1)
    private Integer totalQuantity;

    private Boolean shareable;
}

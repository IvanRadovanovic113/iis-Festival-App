package com.festivalapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdRejectionRequest {

    @NotBlank
    private String reason;
}

package com.festivalapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManagerAdUpdateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String description;
}

package com.festivalapp.dto;

import com.festivalapp.model.FestivalStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class FestivalRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String location;

    @NotNull
    private FestivalStatus status;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;
}

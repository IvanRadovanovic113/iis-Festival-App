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
    private String naziv;

    @NotBlank
    private String lokacija;

    @NotNull
    private FestivalStatus status;

    @NotNull
    private LocalDate datumPocetka;

    @NotNull
    private LocalDate datumZavrsetka;
}

package com.festivalapp.eventorganization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class EventReservationCreateRequest {

    @NotBlank
    private String performerName;

    @NotNull
    private Long stageId;

    @NotNull
    private LocalDate performanceDate;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    private String notes;
}

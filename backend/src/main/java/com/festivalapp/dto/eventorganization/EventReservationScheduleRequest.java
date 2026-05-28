package com.festivalapp.dto.eventorganization;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class EventReservationScheduleRequest {
    @NotNull
    private LocalTime startTime;
}

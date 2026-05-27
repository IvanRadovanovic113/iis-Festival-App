package com.festivalapp.dto.eventorganization;

import java.time.LocalDate;
import java.time.LocalTime;

public record TimetableSlotResponse(
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    String status,
    String performerName
) {}

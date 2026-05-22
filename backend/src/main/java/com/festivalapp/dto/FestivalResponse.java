package com.festivalapp.dto;

import com.festivalapp.model.Festival;
import com.festivalapp.model.FestivalStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class FestivalResponse {

    private Long festivalId;
    private String name;
    private String location;
    private FestivalStatus status;
    private LocalDate startDate;
    private LocalDate endDate;

    public static FestivalResponse from(Festival festival) {
        FestivalResponse response = new FestivalResponse();
        response.festivalId = festival.getFestivalId();
        response.name = festival.getName();
        response.location = festival.getLocation();
        response.status = festival.getStatus();
        response.startDate = festival.getStartDate();
        response.endDate = festival.getEndDate();
        return response;
    }
}

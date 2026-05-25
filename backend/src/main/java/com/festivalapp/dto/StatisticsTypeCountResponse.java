package com.festivalapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StatisticsTypeCountResponse {

    private Long adTypeId;
    private String name;
    private long count;
}

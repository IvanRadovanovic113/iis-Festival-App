package com.festivalapp.dto;

import com.festivalapp.model.AdType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StatisticsAdTypeOptionResponse {

    private Long adTypeId;
    private String name;

    public static StatisticsAdTypeOptionResponse from(AdType adType) {
        return new StatisticsAdTypeOptionResponse(adType.getAdTypeId(), adType.getName());
    }
}

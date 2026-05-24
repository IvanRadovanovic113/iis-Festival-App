package com.festivalapp.dto;

import com.festivalapp.model.AdType;
import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;
import java.util.List;

@Getter
@Setter
public class AdTypeResponse {

    private Long adTypeId;
    private String name;
    private String description;
    private String contentType;
    private List<AdPhaseResponse> phases;

    public static AdTypeResponse from(AdType adType) {
        AdTypeResponse response = new AdTypeResponse();
        response.adTypeId = adType.getAdTypeId();
        response.name = adType.getName();
        response.description = adType.getDescription();
        response.contentType = adType.getContentType();
        response.phases = adType.getPhases().stream()
            .sorted(Comparator.comparing(phase -> phase.getOrderIndex()))
            .map(AdPhaseResponse::from)
            .toList();
        return response;
    }
}

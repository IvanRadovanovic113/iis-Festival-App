package com.festivalapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AdReviewResponse {

    private AdResponse ad;
    private List<AdPhaseResponse> flow;
    private List<AdVersionSummaryResponse> versions;
}

package com.festivalapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CampaignWorkspaceResponse {

    private CampaignResponse campaign;
    private CampaignStatsResponse stats;
    private List<AdResponse> ads;
}

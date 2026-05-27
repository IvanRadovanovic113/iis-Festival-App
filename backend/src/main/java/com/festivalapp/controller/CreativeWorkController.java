package com.festivalapp.controller;

import com.festivalapp.dto.AdResponse;
import com.festivalapp.dto.CreativeCampaignResponse;
import com.festivalapp.dto.CreativeAdUpdateRequest;
import com.festivalapp.model.User;
import com.festivalapp.service.CreativeWorkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/creative")
@RequiredArgsConstructor
public class CreativeWorkController {

    private final CreativeWorkService creativeWorkService;

    @GetMapping("/campaigns")
    public ResponseEntity<List<CreativeCampaignResponse>> getCampaigns(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(creativeWorkService.getCampaigns(user));
    }

    @GetMapping("/campaigns/{campaignId}/ads")
    public ResponseEntity<List<AdResponse>> getCampaignAds(@PathVariable Long campaignId, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(creativeWorkService.getCampaignAds(campaignId, user));
    }

    @GetMapping("/campaigns/{campaignId}/ads/{adId}")
    public ResponseEntity<AdResponse> getAd(
        @PathVariable Long campaignId,
        @PathVariable Long adId,
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(creativeWorkService.getAd(campaignId, adId, user));
    }

    @PutMapping("/campaigns/{campaignId}/ads/{adId}")
    public ResponseEntity<AdResponse> updateAd(
        @PathVariable Long campaignId,
        @PathVariable Long adId,
        @Valid @RequestBody CreativeAdUpdateRequest request,
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(creativeWorkService.updateAd(campaignId, adId, request, user));
    }
}

package com.festivalapp.controller;

import com.festivalapp.dto.CampaignRequest;
import com.festivalapp.dto.CampaignManagerOptionResponse;
import com.festivalapp.dto.AdPromotionRequest;
import com.festivalapp.dto.CampaignResponse;
import com.festivalapp.dto.CampaignWorkspaceResponse;
import com.festivalapp.dto.FestivalCampaignOverviewResponse;
import com.festivalapp.dto.AdRejectionRequest;
import com.festivalapp.dto.AdResponse;
import com.festivalapp.model.Role;
import com.festivalapp.model.User;
import com.festivalapp.service.AdWorkflowService;
import com.festivalapp.service.AdPromotionService;
import com.festivalapp.service.CampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/festival-director")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;
    private final AdWorkflowService adWorkflowService;
    private final AdPromotionService adPromotionService;

    @GetMapping("/festivals")
    public ResponseEntity<List<FestivalCampaignOverviewResponse>> getFestivalOverviews(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(campaignService.getFestivalOverviews(user));
    }

    @GetMapping("/festivals/{festivalId}/campaign")
    public ResponseEntity<CampaignResponse> getCampaign(@PathVariable Long festivalId, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(campaignService.getCampaignByFestival(festivalId, user));
    }

    @GetMapping("/festivals/{festivalId}/campaign/workspace")
    public ResponseEntity<CampaignWorkspaceResponse> getCampaignWorkspace(@PathVariable Long festivalId, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(campaignService.getCampaignWorkspace(festivalId, user));
    }

    @GetMapping("/festivals/{festivalId}/managers")
    public ResponseEntity<List<CampaignManagerOptionResponse>> getManagers(@PathVariable Long festivalId, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(campaignService.getFestivalManagers(festivalId, user));
    }

    @PostMapping("/festivals/{festivalId}/campaign")
    public ResponseEntity<CampaignResponse> createCampaign(
        @PathVariable Long festivalId,
        @Valid @RequestBody CampaignRequest request,
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(campaignService.createCampaign(festivalId, request, user));
    }

    @PutMapping("/festivals/{festivalId}/campaign")
    public ResponseEntity<CampaignResponse> updateCampaign(
        @PathVariable Long festivalId,
        @Valid @RequestBody CampaignRequest request,
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(campaignService.updateCampaign(festivalId, request, user));
    }

    @DeleteMapping("/festivals/{festivalId}/campaign")
    public ResponseEntity<Void> deleteCampaign(
        @PathVariable Long festivalId,
        @AuthenticationPrincipal User user
    ) {
        campaignService.deleteCampaign(festivalId, user);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/festivals/{festivalId}/ads/{adId}/approve")
    public ResponseEntity<AdResponse> approveAd(
        @PathVariable Long festivalId,
        @PathVariable Long adId,
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(adWorkflowService.approve(festivalId, adId, Role.FESTIVAL_DIRECTOR, user));
    }

    @PutMapping("/festivals/{festivalId}/ads/{adId}/reject")
    public ResponseEntity<AdResponse> rejectAd(
        @PathVariable Long festivalId,
        @PathVariable Long adId,
        @Valid @RequestBody AdRejectionRequest request,
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(adWorkflowService.reject(festivalId, adId, request.getReason(), Role.FESTIVAL_DIRECTOR, user));
    }

    @PutMapping("/festivals/{festivalId}/ads/{adId}/promotion")
    public ResponseEntity<AdResponse> savePromotion(
        @PathVariable Long festivalId,
        @PathVariable Long adId,
        @Valid @RequestBody AdPromotionRequest request,
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(adPromotionService.savePromotion(festivalId, adId, request, user));
    }
}

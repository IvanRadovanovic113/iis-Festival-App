package com.festivalapp.controller;

import com.festivalapp.dto.*;
import com.festivalapp.model.User;
import com.festivalapp.service.FestivalManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/festival-manager")
@RequiredArgsConstructor
public class FestivalManagerController {

    private final FestivalManagerService festivalManagerService;

    @GetMapping("/festivals")
    public ResponseEntity<List<FestivalCampaignOverviewResponse>> getFestivalOverviews(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(festivalManagerService.getFestivalOverviews(user));
    }

    @GetMapping("/festivals/{festivalId}/campaign")
    public ResponseEntity<CampaignWorkspaceResponse> getCampaignWorkspace(@PathVariable Long festivalId, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(festivalManagerService.getCampaignWorkspace(festivalId, user));
    }

    @GetMapping("/ad-types")
    public ResponseEntity<List<AdTypeResponse>> getAdTypes(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(festivalManagerService.getAdTypes(user));
    }

    @GetMapping("/ad-phases")
    public ResponseEntity<List<AdPhaseResponse>> getAdPhases(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(festivalManagerService.getPhases(user));
    }

    @PostMapping("/campaigns/{campaignId}/ads")
    public ResponseEntity<AdResponse> createAd(
        @PathVariable Long campaignId,
        @Valid @RequestBody AdRequest request,
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(festivalManagerService.createAd(campaignId, request, user));
    }

    @PostMapping("/ad-types")
    public ResponseEntity<AdTypeResponse> createAdType(
        @Valid @RequestBody AdTypeRequest request,
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(festivalManagerService.createAdType(request, user));
    }

    @PostMapping("/ad-phases")
    public ResponseEntity<AdPhaseResponse> createAdPhase(
        @Valid @RequestBody AdPhaseRequest request,
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(festivalManagerService.createPhase(request, user));
    }
}

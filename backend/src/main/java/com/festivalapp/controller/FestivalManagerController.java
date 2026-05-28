package com.festivalapp.controller;

import com.festivalapp.dto.*;
import com.festivalapp.model.User;
import com.festivalapp.model.Role;
import com.festivalapp.service.AdWorkflowService;
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
    private final AdWorkflowService adWorkflowService;

    @GetMapping("/festivals")
    public ResponseEntity<List<FestivalCampaignOverviewResponse>> getFestivalOverviews(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(festivalManagerService.getFestivalOverviews(user));
    }

    @GetMapping("/festivals/{festivalId}/campaign")
    public ResponseEntity<CampaignWorkspaceResponse> getCampaignWorkspace(@PathVariable Long festivalId, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(festivalManagerService.getCampaignWorkspace(festivalId, user));
    }

    @GetMapping("/festivals/{festivalId}/ads/{adId}")
    public ResponseEntity<AdResponse> getAd(
        @PathVariable Long festivalId,
        @PathVariable Long adId,
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(festivalManagerService.getAd(festivalId, adId, user));
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

    @PutMapping("/festivals/{festivalId}/ads/{adId}")
    public ResponseEntity<AdResponse> updateAd(
        @PathVariable Long festivalId,
        @PathVariable Long adId,
        @Valid @RequestBody ManagerAdUpdateRequest request,
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(festivalManagerService.updateAd(festivalId, adId, request, user));
    }

    @DeleteMapping("/festivals/{festivalId}/ads/{adId}")
    public ResponseEntity<Void> deleteAd(
        @PathVariable Long festivalId,
        @PathVariable Long adId,
        @AuthenticationPrincipal User user
    ) {
        festivalManagerService.deleteAd(festivalId, adId, user);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/festivals/{festivalId}/ads/{adId}/approve")
    public ResponseEntity<AdResponse> approveAd(
        @PathVariable Long festivalId,
        @PathVariable Long adId,
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(adWorkflowService.approve(festivalId, adId, Role.FESTIVAL_MANAGER, user));
    }

    @PutMapping("/festivals/{festivalId}/ads/{adId}/reject")
    public ResponseEntity<AdResponse> rejectAd(
        @PathVariable Long festivalId,
        @PathVariable Long adId,
        @Valid @RequestBody AdRejectionRequest request,
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(adWorkflowService.reject(festivalId, adId, request.getReason(), Role.FESTIVAL_MANAGER, user));
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

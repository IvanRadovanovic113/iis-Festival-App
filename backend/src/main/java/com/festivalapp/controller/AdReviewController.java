package com.festivalapp.controller;

import com.festivalapp.dto.AdReviewResponse;
import com.festivalapp.dto.AdVersionDetailResponse;
import com.festivalapp.model.User;
import com.festivalapp.service.AdReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ad-reviews")
@RequiredArgsConstructor
public class AdReviewController {

    private final AdReviewService adReviewService;

    @GetMapping("/festivals/{festivalId}/ads/{adId}")
    public ResponseEntity<AdReviewResponse> getAdReview(
        @PathVariable Long festivalId,
        @PathVariable Long adId,
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(adReviewService.getAdReview(festivalId, adId, user));
    }

    @GetMapping("/festivals/{festivalId}/ads/{adId}/versions/{versionNumber}")
    public ResponseEntity<AdVersionDetailResponse> getVersionDetail(
        @PathVariable Long festivalId,
        @PathVariable Long adId,
        @PathVariable Integer versionNumber,
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(adReviewService.getVersionDetail(festivalId, adId, versionNumber, user));
    }
}

package com.festivalapp.controller;

import com.festivalapp.dto.AdResponse;
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

    @GetMapping("/ads")
    public ResponseEntity<List<AdResponse>> getAssignedAds(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(creativeWorkService.getAssignedAds(user));
    }

    @GetMapping("/ads/{adId}")
    public ResponseEntity<AdResponse> getAd(@PathVariable Long adId, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(creativeWorkService.getAd(adId, user));
    }

    @PutMapping("/ads/{adId}")
    public ResponseEntity<AdResponse> updateAd(
        @PathVariable Long adId,
        @Valid @RequestBody CreativeAdUpdateRequest request,
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(creativeWorkService.updateAd(adId, request, user));
    }
}

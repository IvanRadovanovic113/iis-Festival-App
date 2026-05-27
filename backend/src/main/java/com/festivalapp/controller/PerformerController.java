package com.festivalapp.controller;

import com.festivalapp.dto.PerformerRequest;
import com.festivalapp.dto.PerformerResponse;
import com.festivalapp.model.PerformerStatus;
import com.festivalapp.model.PerformerType;
import com.festivalapp.model.User;
import com.festivalapp.service.PerformerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/negotiation-manager/performers")
@RequiredArgsConstructor
public class PerformerController {

    private final PerformerService performerService;

    @PostMapping
    public ResponseEntity<PerformerResponse> createPerformer(
            @Valid @RequestBody PerformerRequest request,
            @AuthenticationPrincipal User user
    ) {
        PerformerResponse response = performerService.createPerformer(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Primer poziva: /api/negotiation-manager/performers?genre=Rock&performerType=BAND&searchName=Dubioza&page=0&size=10
    @GetMapping
    public ResponseEntity<Page<PerformerResponse>> getPerformers(
            @RequestParam(required = false) PerformerStatus status,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) PerformerType performerType,
            @RequestParam(required = false) String countryOfOrigin,
            @RequestParam(required = false) Integer numberOfMembers,
            @RequestParam(required = false) String searchName,
            @PageableDefault(size = 10, sort = "stageName") Pageable pageable,
            @AuthenticationPrincipal User user
    ) {
        Page<PerformerResponse> performers = performerService.getPerformers(
                status, genre, performerType, countryOfOrigin, numberOfMembers, searchName, pageable, user
        );
        return ResponseEntity.ok(performers);
    }

    @GetMapping("/{performerId}")
    public ResponseEntity<PerformerResponse> getPerformerById(
            @PathVariable Long performerId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(performerService.getPerformerById(performerId, user));
    }

    @PutMapping("/{performerId}")
    public ResponseEntity<PerformerResponse> updatePerformer(
            @PathVariable Long performerId,
            @Valid @RequestBody PerformerRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(performerService.updatePerformer(performerId, request, user));
    }

    @PatchMapping("/{performerId}/archive")
    public ResponseEntity<PerformerResponse> archivePerformer(
            @PathVariable Long performerId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(performerService.archivePerformer(performerId, user));
    }
}
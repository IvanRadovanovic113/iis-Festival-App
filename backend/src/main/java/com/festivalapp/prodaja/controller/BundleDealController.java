package com.festivalapp.prodaja.controller;

import com.festivalapp.model.User;
import com.festivalapp.prodaja.dto.BundleDealRequest;
import com.festivalapp.prodaja.dto.BundleDealResponse;
import com.festivalapp.prodaja.service.BundleDealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BundleDealController {

    private final BundleDealService bundleDealService;

    @GetMapping("/api/festivals/{festivalId}/bundle-deals")
    public ResponseEntity<List<BundleDealResponse>> getAll(
            @PathVariable Long festivalId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bundleDealService.getAll(user));
    }

    @PostMapping("/api/festivals/{festivalId}/bundle-deals")
    public ResponseEntity<BundleDealResponse> create(
            @PathVariable Long festivalId,
            @Valid @RequestBody BundleDealRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bundleDealService.create(request, user));
    }

    @PutMapping("/api/bundle-deals/{id}")
    public ResponseEntity<BundleDealResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody BundleDealRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bundleDealService.update(id, request, user));
    }

    @DeleteMapping("/api/bundle-deals/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        bundleDealService.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/bundle-deals/{id}/toggle-active")
    public ResponseEntity<BundleDealResponse> toggleActive(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bundleDealService.toggleActive(id, user));
    }
}

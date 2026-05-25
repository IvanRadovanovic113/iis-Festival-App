package com.festivalapp.prodaja.controller;

import com.festivalapp.model.User;
import com.festivalapp.prodaja.dto.PromoCodeRequest;
import com.festivalapp.prodaja.dto.PromoCodeResponse;
import com.festivalapp.prodaja.service.PromoCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PromoCodeController {

    private final PromoCodeService promoCodeService;

    @GetMapping("/api/festivals/{festivalId}/promo-codes")
    public ResponseEntity<List<PromoCodeResponse>> getAll(
            @PathVariable Long festivalId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(promoCodeService.getAll(user));
    }

    @PostMapping("/api/festivals/{festivalId}/promo-codes")
    public ResponseEntity<PromoCodeResponse> create(
            @PathVariable Long festivalId,
            @Valid @RequestBody PromoCodeRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(promoCodeService.create(request, user));
    }

    @PutMapping("/api/promo-codes/{id}")
    public ResponseEntity<PromoCodeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PromoCodeRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(promoCodeService.update(id, request, user));
    }

    @DeleteMapping("/api/promo-codes/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        promoCodeService.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/promo-codes/{id}/toggle-active")
    public ResponseEntity<PromoCodeResponse> toggleActive(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(promoCodeService.toggleActive(id, user));
    }
}

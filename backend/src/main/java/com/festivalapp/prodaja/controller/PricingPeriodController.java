package com.festivalapp.prodaja.controller;

import com.festivalapp.model.User;
import com.festivalapp.prodaja.dto.PricingPeriodRequest;
import com.festivalapp.prodaja.dto.PricingPeriodResponse;
import com.festivalapp.prodaja.service.PricingPeriodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PricingPeriodController {

    private final PricingPeriodService pricingPeriodService;

    @GetMapping("/api/ticket-types/{ticketTypeId}/price-periods")
    public ResponseEntity<List<PricingPeriodResponse>> getAll(
            @PathVariable Long ticketTypeId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(pricingPeriodService.getAll(ticketTypeId, user));
    }

    @PostMapping("/api/ticket-types/{ticketTypeId}/price-periods")
    public ResponseEntity<PricingPeriodResponse> create(
            @PathVariable Long ticketTypeId,
            @Valid @RequestBody PricingPeriodRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pricingPeriodService.create(ticketTypeId, request, user));
    }

    @PutMapping("/api/ticket-types/{ticketTypeId}/price-periods/{periodId}")
    public ResponseEntity<PricingPeriodResponse> update(
            @PathVariable Long ticketTypeId,
            @PathVariable Long periodId,
            @Valid @RequestBody PricingPeriodRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(pricingPeriodService.update(ticketTypeId, periodId, request, user));
    }

    @DeleteMapping("/api/ticket-types/{ticketTypeId}/price-periods/{periodId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long ticketTypeId,
            @PathVariable Long periodId,
            @AuthenticationPrincipal User user) {
        pricingPeriodService.delete(ticketTypeId, periodId, user);
        return ResponseEntity.noContent().build();
    }
}

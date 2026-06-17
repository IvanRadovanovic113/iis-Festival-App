package com.festivalapp.prodaja.controller;

import com.festivalapp.model.User;
import com.festivalapp.prodaja.dto.OcekivanaProdajaRequest;
import com.festivalapp.prodaja.dto.OcekivanaProdajaResponse;
import com.festivalapp.prodaja.service.OcekivanaProdajaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class OcekivanaProdajaController {

    private final OcekivanaProdajaService ocekivanaProdajaService;

    @GetMapping("/api/price-periods/{periodId}/expected-sales")
    public ResponseEntity<OcekivanaProdajaResponse> get(
            @PathVariable Long periodId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ocekivanaProdajaService.get(periodId, user));
    }

    @PostMapping("/api/price-periods/{periodId}/expected-sales")
    public ResponseEntity<OcekivanaProdajaResponse> upsert(
            @PathVariable Long periodId,
            @Valid @RequestBody OcekivanaProdajaRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ocekivanaProdajaService.upsert(periodId, request, user));
    }

    @PutMapping("/api/price-periods/{periodId}/expected-sales")
    public ResponseEntity<OcekivanaProdajaResponse> update(
            @PathVariable Long periodId,
            @Valid @RequestBody OcekivanaProdajaRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ocekivanaProdajaService.upsert(periodId, request, user));
    }
}

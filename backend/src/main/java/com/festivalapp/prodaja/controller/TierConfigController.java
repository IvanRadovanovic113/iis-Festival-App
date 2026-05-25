package com.festivalapp.prodaja.controller;

import com.festivalapp.prodaja.dto.TierConfigRequest;
import com.festivalapp.prodaja.dto.TierConfigResponse;
import com.festivalapp.prodaja.model.KupacTier;
import com.festivalapp.prodaja.service.TierConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tier-config")
@RequiredArgsConstructor
public class TierConfigController {

    private final TierConfigService tierConfigService;

    @GetMapping
    public ResponseEntity<List<TierConfigResponse>> getAll() {
        return ResponseEntity.ok(tierConfigService.getAll());
    }

    @PutMapping("/{tier}")
    public ResponseEntity<TierConfigResponse> update(
            @PathVariable KupacTier tier,
            @Valid @RequestBody TierConfigRequest request) {
        return ResponseEntity.ok(tierConfigService.update(tier, request));
    }
}

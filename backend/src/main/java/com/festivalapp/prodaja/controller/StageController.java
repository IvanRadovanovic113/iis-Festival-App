package com.festivalapp.prodaja.controller;

import com.festivalapp.model.User;
import com.festivalapp.prodaja.dto.StageRequest;
import com.festivalapp.prodaja.dto.StageResponse;
import com.festivalapp.prodaja.service.StageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stages")
@RequiredArgsConstructor
public class StageController {

    private final StageService stageService;

    @GetMapping
    public ResponseEntity<List<StageResponse>> getAll(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(stageService.getAll(user));
    }

    @PostMapping
    public ResponseEntity<StageResponse> create(
            @Valid @RequestBody StageRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stageService.create(request, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StageResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody StageRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(stageService.update(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        stageService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}

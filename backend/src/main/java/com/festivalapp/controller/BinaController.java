package com.festivalapp.controller;

import com.festivalapp.dto.BinaRequest;
import com.festivalapp.dto.BinaResponse;
import com.festivalapp.model.User;
import com.festivalapp.service.BinaService;
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
public class BinaController {

    private final BinaService binaService;

    @GetMapping
    public ResponseEntity<List<BinaResponse>> getAll(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(binaService.getAll(user));
    }

    @PostMapping
    public ResponseEntity<BinaResponse> create(
            @Valid @RequestBody BinaRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(binaService.create(request, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BinaResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody BinaRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(binaService.update(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        binaService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}

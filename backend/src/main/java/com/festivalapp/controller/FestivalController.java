package com.festivalapp.controller;

import com.festivalapp.dto.FestivalRequest;
import com.festivalapp.dto.FestivalResponse;
import com.festivalapp.service.FestivalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/festivals")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class FestivalController {

    private final FestivalService festivalService;

    @PostMapping
    public ResponseEntity<FestivalResponse> create(@Valid @RequestBody FestivalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(festivalService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<FestivalResponse>> getAll() {
        return ResponseEntity.ok(festivalService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FestivalResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(festivalService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        festivalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

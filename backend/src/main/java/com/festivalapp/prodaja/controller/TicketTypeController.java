package com.festivalapp.prodaja.controller;

import com.festivalapp.model.User;
import com.festivalapp.prodaja.dto.TicketTypeRequest;
import com.festivalapp.prodaja.dto.TicketTypeResponse;
import com.festivalapp.prodaja.service.TicketTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    @GetMapping("/api/festivals/{festivalId}/ticket-types")
    public ResponseEntity<List<TicketTypeResponse>> getAll(
            @PathVariable Long festivalId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ticketTypeService.getAll(user));
    }

    @GetMapping("/api/ticket-types/{id}")
    public ResponseEntity<TicketTypeResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ticketTypeService.getById(id, user));
    }

    @PostMapping("/api/festivals/{festivalId}/ticket-types")
    public ResponseEntity<TicketTypeResponse> create(
            @PathVariable Long festivalId,
            @Valid @RequestBody TicketTypeRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketTypeService.create(request, user));
    }

    @PutMapping("/api/ticket-types/{id}")
    public ResponseEntity<TicketTypeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TicketTypeRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ticketTypeService.update(id, request, user));
    }

    @DeleteMapping("/api/ticket-types/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        ticketTypeService.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/ticket-types/{id}/dynamic-pricing")
    public ResponseEntity<TicketTypeResponse> toggleDynamicPricing(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body,
            @AuthenticationPrincipal User user) {
        boolean active = Boolean.TRUE.equals(body.get("active"));
        return ResponseEntity.ok(ticketTypeService.toggleDynamicPricing(id, active, user));
    }
}

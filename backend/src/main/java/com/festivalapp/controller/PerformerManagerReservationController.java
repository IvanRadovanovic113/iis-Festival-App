package com.festivalapp.controller;

import com.festivalapp.dto.eventorganization.EventReservationResponse;
import com.festivalapp.dto.performermanager.CreateContractReservationRequest;
import com.festivalapp.dto.performermanager.PerformerContractReservationResponse;
import com.festivalapp.model.User;
import com.festivalapp.service.PerformerManagerReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/performer-manager/reservations")
@RequiredArgsConstructor
public class PerformerManagerReservationController {

    private final PerformerManagerReservationService reservationService;

    @GetMapping("/contracts")
    public ResponseEntity<List<PerformerContractReservationResponse>> getContracts(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(reservationService.getContracts(user));
    }

    @PostMapping("/contracts/{contractId}/request")
    public ResponseEntity<EventReservationResponse> createReservationRequest(
            @PathVariable Long contractId,
            @Valid @RequestBody CreateContractReservationRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(reservationService.createReservationRequest(contractId, request, user));
    }
}

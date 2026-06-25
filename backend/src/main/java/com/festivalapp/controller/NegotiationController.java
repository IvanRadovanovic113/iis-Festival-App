package com.festivalapp.controller;

import com.festivalapp.dto.TransitionRequest;
import com.festivalapp.model.Negotiation;
import com.festivalapp.model.NegotiationStatus;
import com.festivalapp.model.User;
import com.festivalapp.service.NegotiationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.festivalapp.dto.*;
import com.festivalapp.dto.NegotiationMapper;
import com.festivalapp.repository.NegotiationStateHistoryRepository;
import com.festivalapp.repository.NegotiationConditionValueRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.festivalapp.dto.FailReasonRequest;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.time.LocalDate;
import com.festivalapp.dto.PerformerStatsDto;
import com.festivalapp.model.PerformerType;

@RestController
@RequestMapping("/api/negotiation-manager/negotiations")
@RequiredArgsConstructor
public class NegotiationController {

    private final NegotiationService negotiationService;
    private final NegotiationStateHistoryRepository historyRepository;
    private final NegotiationConditionValueRepository conditionValueRepository;

    // 1. Inicijacija pregovora
    @PostMapping("/{offerId}/start/{performerId}")
    public ResponseEntity<NegotiationResponse> initiate(
            @PathVariable Long offerId,
            @PathVariable Long performerId,
            @AuthenticationPrincipal User user) {
        Negotiation n = negotiationService.initiateNegotiation(offerId, performerId, user);
        return ResponseEntity.ok(NegotiationMapper.toResponse(n));
    }

    // 2. Detaljan pregled
    @GetMapping("/{negotiationId}")
    public ResponseEntity<NegotiationDetailsResponse> getDetails(
            @PathVariable Long negotiationId,
            @AuthenticationPrincipal User user) {
        
        NegotiationDetailsResponse details = negotiationService.getNegotiationDetails(negotiationId, user);
        
        return ResponseEntity.ok(details);
    }

    // 3. Paginirana lista sa filterima
    @GetMapping
    public ResponseEntity<Page<NegotiationResponse>> getAll(
            @RequestParam(required = false) NegotiationStatus status,
            @RequestParam(required = false) Long performerId,
            @RequestParam(required = false) Long offerId,
            @RequestParam(required = false) Long stateId,
            Pageable pageable,
            @AuthenticationPrincipal User user) {
        Page<Negotiation> page = negotiationService.getNegotiations(status, performerId, offerId, stateId, pageable, user);
        return ResponseEntity.ok(page.map(NegotiationMapper::toResponse));
    }

    // 4. Unos uslova pregovora
    @PostMapping("/{negotiationId}/conditions")
    public ResponseEntity<Void> saveConditions(
            @PathVariable Long negotiationId,
            @RequestBody List<ConditionValueDto> conditions,
            @AuthenticationPrincipal User user) {
        negotiationService.saveNegotiationConditions(negotiationId, conditions, user);
        return ResponseEntity.ok().build();
    }

    // 5. tranzicija stanja
    @PostMapping("/{negotiationId}/transition/{transitionId}")
    public ResponseEntity<Void> transition(
            @PathVariable Long negotiationId,
            @PathVariable Long transitionId,
            @AuthenticationPrincipal User user) {
        TransitionRequest request = new TransitionRequest();
        request.setTransitionId(transitionId);
        negotiationService.transitionToNextState(negotiationId, request, user);
        return ResponseEntity.ok().build();
    }

    // 6.. Završetak pregovora (Ugovor)
    @PostMapping("/{negotiationId}/complete")
    public ResponseEntity<Void> complete(
            @PathVariable Long negotiationId,
            @AuthenticationPrincipal User user) {
        negotiationService.completeNegotiation(negotiationId, user);
        return ResponseEntity.ok().build();
    }

    // 7.. Prekid pregovora
    @PostMapping("/{negotiationId}/fail")
    public ResponseEntity<Void> fail(
            @PathVariable Long negotiationId,
            @RequestBody FailReasonRequest request,
            @AuthenticationPrincipal User user) {
        negotiationService.failNegotiation(negotiationId, request.getReason(), user);
        return ResponseEntity.ok().build();
    }
}
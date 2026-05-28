package com.festivalapp.controller;

import com.festivalapp.dto.TransitionConditionRequest;
import com.festivalapp.dto.TransitionConditionResponse;
import com.festivalapp.model.User;
import com.festivalapp.service.ConditionCatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // VRAĆENO
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/negotiation-manager/workflow-conditions")
@RequiredArgsConstructor
public class ConditionCatalogController {

    private final ConditionCatalogService conditionCatalogService;

    // 1. Dodavanje novog uslova u globalni katalog šablona
    @PostMapping
    public ResponseEntity<TransitionConditionResponse> createCatalogCondition(
            @Valid @RequestBody TransitionConditionRequest request,
            @AuthenticationPrincipal User user // VRAĆENO na original pošto filter radi perfektno
    ) {
        TransitionConditionResponse response = conditionCatalogService.createCatalogCondition(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. Pregled i pretraga uslova iz catalogs uz paginaciju
    @GetMapping
    public ResponseEntity<Page<TransitionConditionResponse>> getCatalogConditions(
            @RequestParam(required = false) String searchTerm,
            @PageableDefault(size = 10, sort = "label") Pageable pageable,
            @AuthenticationPrincipal User user // VRAĆENO
    ) {
        Page<TransitionConditionResponse> conditions = conditionCatalogService.getCatalogConditions(searchTerm, pageable, user);
        return ResponseEntity.ok(conditions);
    }
}
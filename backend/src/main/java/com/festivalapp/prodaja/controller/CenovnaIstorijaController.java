package com.festivalapp.prodaja.controller;

import com.festivalapp.model.User;
import com.festivalapp.prodaja.dto.CenovnaIstorijaResponse;
import com.festivalapp.prodaja.service.CenovnaIstorijaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CenovnaIstorijaController {

    private final CenovnaIstorijaService cenovnaIstorijaService;

    @GetMapping("/api/ticket-types/{ticketTypeId}/price-history")
    public ResponseEntity<Page<CenovnaIstorijaResponse>> getHistory(
            @PathVariable Long ticketTypeId,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(cenovnaIstorijaService.getHistory(ticketTypeId, user, pageable));
    }
}

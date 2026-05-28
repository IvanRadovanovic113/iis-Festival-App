package com.festivalapp.controller;

import com.festivalapp.dto.OfferDetailResponse;
import com.festivalapp.dto.OfferRequest;
import com.festivalapp.dto.OfferResponse;
import com.festivalapp.model.OfferStatus;
import com.festivalapp.model.User;
import com.festivalapp.service.OfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/negotiation-manager/offers")
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;

    // 1. Kreiranje nove ponude (Jira FEST 159: Ponuda se kreira u statusu "DRAFT")
    @PostMapping
    public ResponseEntity<OfferDetailResponse> createOffer(
            @Valid @RequestBody OfferRequest request,
            @AuthenticationPrincipal User user
    ) {
        OfferDetailResponse response = offerService.createOffer(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. Izmena neobjavljene ponude (Jira FEST 160: Dozvoljeno samo za status "DRAFT")
    @PutMapping("/{offerId}")
    public ResponseEntity<OfferDetailResponse> updateOffer(
            @PathVariable Long offerId,
            @Valid @RequestBody OfferRequest request,
            @AuthenticationPrincipal User user
    ) {
        OfferDetailResponse response = offerService.updateOffer(offerId, request, user);
        return ResponseEntity.ok(response);
    }

    // 3. Objavljivanje kreirane ponude (Jira FEST 161: Prelazak u status "PUBLISHED" + vreme)
    @PatchMapping("/{offerId}/publish")
    public ResponseEntity<OfferDetailResponse> publishOffer(
            @PathVariable Long offerId,
            @AuthenticationPrincipal User user
    ) {
        OfferDetailResponse response = offerService.publishOffer(offerId, user);
        return ResponseEntity.ok(response);
    }

    // 4. Pregled svih ponuda grupisanih/filtriranih po jednom statusu uz paginaciju(Jira FEST 162)
    // Primer poziva: /api/negotiation-manager/offers?status=DRAFT&page=0&size=10
    @GetMapping
    public ResponseEntity<Page<OfferResponse>> getOffers(
            @RequestParam(required = false) OfferStatus status,
            @RequestParam(required = false) String searchTerm,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal User user
    ) {
        Page<OfferResponse> offers = offerService.getOffers(status, searchTerm, pageable, user);
        return ResponseEntity.ok(offers);
    }

    // 5. Pregled svih detalja konkretne ponude (Jira FEST 163)
    @GetMapping("/{offerId}")
    public ResponseEntity<OfferDetailResponse> getOfferById(
            @PathVariable Long offerId,
            @AuthenticationPrincipal User user
    ) {
        OfferDetailResponse response = offerService.getOfferById(offerId, user);
        return ResponseEntity.ok(response);
    }

    // 6. Arhiviranje ponude (Jira FEST 164: Dozvoljeno za "DRAFT" i "PUBLISHED")
    @PatchMapping("/{offerId}/archive")
    public ResponseEntity<OfferDetailResponse> archiveOffer(
            @PathVariable Long offerId,
            @AuthenticationPrincipal User user
    ) {
        OfferDetailResponse response = offerService.archiveOffer(offerId, user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{offerId}/interested-performers/{performerId}")
    public ResponseEntity<Void> addInterestedPerformer(
            @PathVariable Long offerId,
            @PathVariable Long performerId) {
        offerService.addInterestedPerformer(offerId, performerId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{offerId}/interested-performers/{performerId}")
    public ResponseEntity<Void> removeInterestedPerformer(
            @PathVariable Long offerId,
            @PathVariable Long performerId) {
        offerService.removeInterestedPerformer(offerId, performerId);
        return ResponseEntity.ok().build();
    }
}
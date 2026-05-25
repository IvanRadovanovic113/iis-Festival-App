package com.festivalapp.prodaja.controller;

import com.festivalapp.model.User;
import com.festivalapp.prodaja.dto.CheckoutPreviewResponse;
import com.festivalapp.prodaja.dto.PurchaseRequest;
import com.festivalapp.prodaja.dto.PurchaseResponse;
import com.festivalapp.prodaja.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    /**
     * Pregled cene pre kupovine — ne menja stanje baze.
     * Validira promo kod, računa popuste i gratis karte.
     */
    @PostMapping("/preview")
    public ResponseEntity<CheckoutPreviewResponse> preview(
            @Valid @RequestBody PurchaseRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(purchaseService.preview(request, user));
    }

    /**
     * Vraća sve kupovine ulogovanog kupca, sortirано od najnovije.
     */
    @GetMapping("/my-purchases")
    public ResponseEntity<List<PurchaseResponse>> myPurchases(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(purchaseService.getMyPurchases(user));
    }

    /**
     * Izvršava kupovinu — kreira Kupovinu i Karte.
     */
    @PostMapping("/purchase")
    public ResponseEntity<PurchaseResponse> purchase(
            @Valid @RequestBody PurchaseRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(purchaseService.purchase(request, user));
    }
}

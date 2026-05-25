package com.festivalapp.prodaja.controller;

import com.festivalapp.prodaja.dto.ShopTicketTypeResponse;
import com.festivalapp.prodaja.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @GetMapping("/ticket-types")
    public ResponseEntity<List<ShopTicketTypeResponse>> getAvailableTicketTypes() {
        return ResponseEntity.ok(shopService.getAvailableTicketTypes());
    }
}

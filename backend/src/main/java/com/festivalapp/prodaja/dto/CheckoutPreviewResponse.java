package com.festivalapp.prodaja.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CheckoutPreviewResponse {

    private Long ticketTypeId;
    private String ticketTypeName;

    private BigDecimal pricePerTicket;
    private Integer quantityPaid;
    private BigDecimal baseTotal;          // pricePerTicket × quantityPaid

    private String promoCodeApplied;       // null ako nije primenjen
    private Integer promoDiscountPercent;  // 0 ako nema

    private String tierName;               // BRONZE / SILVER / GOLD
    private Integer tierDiscountPercent;   // 5 / 10 / 15

    private Integer totalDiscountPercent;  // promo + tier, max 100

    private Integer freeTickets;           // gratis karte od bundle deal-a
    private String bundleDealDescription;  // npr. "Kupi 3, dobij 1 gratis" — null ako nema

    private Integer totalTickets;          // quantityPaid + freeTickets
    private BigDecimal finalPrice;         // baseTotal × (1 - totalDiscountPercent/100)

    private Integer availableCount;        // koliko karata još ima na stanju
}

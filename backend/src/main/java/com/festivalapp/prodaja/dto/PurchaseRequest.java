package com.festivalapp.prodaja.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseRequest {

    @NotNull
    private Long ticketTypeId;

    @NotNull
    @Min(1)
    private Integer quantity;

    /** Opcioni promo kod — null ili prazan string znači "bez koda" */
    private String promoCode;
}

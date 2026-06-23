package com.festivalapp.prodaja.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class SalesByTicketTypeDto {
    private Long ticketTypeId;
    private String name;
    private Integer soldCount;
    private BigDecimal revenue;
    private Integer totalQuantity;
}

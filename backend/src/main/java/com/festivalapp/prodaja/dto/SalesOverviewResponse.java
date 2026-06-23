package com.festivalapp.prodaja.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class SalesOverviewResponse {
    private String festivalName;
    private BigDecimal totalRevenue;
    private Integer totalTicketsSold;
    private Integer totalTicketsAvailable;
    private Integer activeTicketTypes;
    private List<TicketTypeSummaryDto> ticketTypes;
    private boolean salesWarning;
    private List<String> warningMessages;
    private boolean salesHighlight;
    private List<String> highlightMessages;
}

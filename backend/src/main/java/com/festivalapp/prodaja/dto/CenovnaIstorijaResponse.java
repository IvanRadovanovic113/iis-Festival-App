package com.festivalapp.prodaja.dto;

import com.festivalapp.prodaja.model.CenovnaIstorija;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CenovnaIstorijaResponse {

    private Long cenovnaIstorijaId;
    private Long ticketTypeId;
    private Long pricingPeriodId;
    private LocalDateTime datum;
    private BigDecimal staraCena;
    private BigDecimal novaCena;
    private String razlog;
    private Boolean jeRucnaPromena;

    public static CenovnaIstorijaResponse from(CenovnaIstorija c) {
        CenovnaIstorijaResponse r = new CenovnaIstorijaResponse();
        r.cenovnaIstorijaId = c.getCenovnaIstorijaId();
        r.ticketTypeId = c.getTicketType().getTicketTypeId();
        r.pricingPeriodId = c.getPricingPeriod().getPricingPeriodId();
        r.datum = c.getDatum();
        r.staraCena = c.getStaraCena();
        r.novaCena = c.getNovaCena();
        r.razlog = c.getRazlog();
        r.jeRucnaPromena = c.getJeRucnaPromena();
        return r;
    }
}

package com.festivalapp.prodaja.dto;

import com.festivalapp.prodaja.model.BundleDeal;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BundleDealResponse {

    private Long akcijaId;
    private Integer kupiKarata;
    private Integer dobijaKarata;
    private LocalDate vaziOd;
    private LocalDate vaziDo;
    private Integer dostupnoAkcija;
    private Integer usedCount;
    private Boolean active;
    private Long ticketTypeId;
    private String ticketTypeName;
    private Long festivalId;

    /** Computed: ACTIVE, PAUSED, EXPIRED, EXHAUSTED */
    private String status;

    public static BundleDealResponse from(BundleDeal b) {
        BundleDealResponse r = new BundleDealResponse();
        r.akcijaId = b.getAkcijaId();
        r.kupiKarata = b.getKupiKarata();
        r.dobijaKarata = b.getDobijaKarata();
        r.vaziOd = b.getVaziOd();
        r.vaziDo = b.getVaziDo();
        r.dostupnoAkcija = b.getDostupnoAkcija();
        r.usedCount = b.getUsedCount();
        r.active = b.getActive();
        r.ticketTypeId = b.getTicketType().getTicketTypeId();
        r.ticketTypeName = b.getTicketType().getName();
        r.festivalId = b.getTicketType().getFestival().getFestivalId();
        r.status = computeStatus(b);
        return r;
    }

    private static String computeStatus(BundleDeal b) {
        if (!Boolean.TRUE.equals(b.getActive())) return "PAUSED";
        if (LocalDate.now().isAfter(b.getVaziDo())) return "EXPIRED";
        if (b.getUsedCount() >= b.getDostupnoAkcija()) return "EXHAUSTED";
        return "ACTIVE";
    }
}

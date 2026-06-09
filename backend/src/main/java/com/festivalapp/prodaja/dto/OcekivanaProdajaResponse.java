package com.festivalapp.prodaja.dto;

import com.festivalapp.prodaja.model.OcekivanaProdaja;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OcekivanaProdajaResponse {

    private Long ocekivanaProdajaId;
    private Long pricingPeriodId;
    private Integer brojKarata;
    private Integer brojSati;
    private BigDecimal agresivnost;
    private Integer intervalMinuti;
    private Integer scarcityPragNizak;
    private Integer scarcityPragVisok;
    private BigDecimal scarcityMultiplikatorNizak;
    private BigDecimal scarcityMultiplikatorVisok;

    public static OcekivanaProdajaResponse from(OcekivanaProdaja o) {
        OcekivanaProdajaResponse r = new OcekivanaProdajaResponse();
        r.ocekivanaProdajaId = o.getOcekivanaProdajaId();
        r.pricingPeriodId = o.getPricingPeriod().getPricingPeriodId();
        r.brojKarata = o.getBrojKarata();
        r.brojSati = o.getBrojSati();
        r.agresivnost = o.getAgresivnost();
        r.intervalMinuti = o.getIntervalMinuti();
        r.scarcityPragNizak = o.getScarcityPragNizak();
        r.scarcityPragVisok = o.getScarcityPragVisok();
        r.scarcityMultiplikatorNizak = o.getScarcityMultiplikatorNizak();
        r.scarcityMultiplikatorVisok = o.getScarcityMultiplikatorVisok();
        return r;
    }
}

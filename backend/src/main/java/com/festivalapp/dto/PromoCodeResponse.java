package com.festivalapp.dto;

import com.festivalapp.model.PromoCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PromoCodeResponse {

    private Long promoCodeId;
    private String code;
    private Integer discountPercent;
    private LocalDate validFrom;
    private LocalDate validTo;
    private Integer maxUses;
    private Integer usedCount;
    private Boolean active;
    private Long festivalId;

    /**
     * Computed status: ACTIVE, INACTIVE (manually disabled), EXPIRED (past validTo),
     * EXHAUSTED (usedCount >= maxUses)
     */
    private String status;

    public static PromoCodeResponse from(PromoCode p) {
        PromoCodeResponse r = new PromoCodeResponse();
        r.promoCodeId = p.getPromoCodeId();
        r.code = p.getCode();
        r.discountPercent = p.getDiscountPercent();
        r.validFrom = p.getValidFrom();
        r.validTo = p.getValidTo();
        r.maxUses = p.getMaxUses();
        r.usedCount = p.getUsedCount();
        r.active = p.getActive();
        r.festivalId = p.getFestival().getFestivalId();
        r.status = computeStatus(p);
        return r;
    }

    private static String computeStatus(PromoCode p) {
        if (!Boolean.TRUE.equals(p.getActive())) return "INACTIVE";
        LocalDate today = LocalDate.now();
        if (today.isAfter(p.getValidTo())) return "EXPIRED";
        if (p.getMaxUses() != null && p.getUsedCount() >= p.getMaxUses()) return "EXHAUSTED";
        return "ACTIVE";
    }
}

package com.festivalapp.prodaja.dto;

import com.festivalapp.prodaja.model.BundleDeal;
import com.festivalapp.prodaja.model.PricingPeriod;
import com.festivalapp.prodaja.model.TicketType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ShopTicketTypeResponse {

    private Long ticketTypeId;
    private String name;
    private Long festivalId;
    private String festivalName;
    private Integer totalQuantity;
    private Integer soldCount;
    private Integer available;
    private BigDecimal currentPrice;
    private List<BundleInfo> activeBundles;

    public record BundleInfo(Integer kupiKarata, Integer dobijaKarata) {}

    public static ShopTicketTypeResponse from(TicketType tt, PricingPeriod activePeriod, List<BundleDeal> bundles) {
        ShopTicketTypeResponse r = new ShopTicketTypeResponse();
        r.ticketTypeId = tt.getTicketTypeId();
        r.name = tt.getName();
        r.festivalId = tt.getFestival().getFestivalId();
        r.festivalName = tt.getFestival().getName();
        r.totalQuantity = tt.getTotalQuantity();
        r.soldCount = tt.getSoldCount();
        r.available = tt.getTotalQuantity() - tt.getSoldCount();
        r.currentPrice = activePeriod != null ? activePeriod.getBasePrice() : null;
        r.activeBundles = bundles.stream()
            .map(b -> new BundleInfo(b.getKupiKarata(), b.getDobijaKarata()))
            .toList();
        return r;
    }
}

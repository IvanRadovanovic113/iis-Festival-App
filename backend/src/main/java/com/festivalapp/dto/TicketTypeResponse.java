package com.festivalapp.dto;

import com.festivalapp.model.TicketType;
import com.festivalapp.model.TicketTypeSegment;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TicketTypeResponse {

    private Long ticketTypeId;
    private String name;
    private Integer totalQuantity;
    private Integer soldCount;
    private Boolean dynamicPricingActive;
    private List<SegmentInfo> segments;
    private Long festivalId;
    private String festivalName;

    public record SegmentInfo(Long segmentId, String name) {}

    public static TicketTypeResponse from(TicketType tt, List<TicketTypeSegment> segments) {
        TicketTypeResponse r = new TicketTypeResponse();
        r.ticketTypeId = tt.getTicketTypeId();
        r.name = tt.getName();
        r.totalQuantity = tt.getTotalQuantity();
        r.soldCount = tt.getSoldCount();
        r.dynamicPricingActive = tt.getDynamicPricingActive();
        r.festivalId = tt.getFestival().getFestivalId();
        r.festivalName = tt.getFestival().getName();
        r.segments = segments.stream()
            .map(s -> new SegmentInfo(
                s.getSegment().getSegmentId(),
                s.getSegment().getName()))
            .toList();
        return r;
    }
}

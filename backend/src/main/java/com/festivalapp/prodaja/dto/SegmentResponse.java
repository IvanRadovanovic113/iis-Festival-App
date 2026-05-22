package com.festivalapp.prodaja.dto;

import com.festivalapp.model.Segment;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SegmentResponse {

    private Long segmentId;
    private String name;
    private Long festivalId;
    private String festivalName;

    public static SegmentResponse from(Segment s) {
        SegmentResponse r = new SegmentResponse();
        r.segmentId = s.getSegmentId();
        r.name = s.getName();
        r.festivalId = s.getFestival().getFestivalId();
        r.festivalName = s.getFestival().getName();
        return r;
    }
}

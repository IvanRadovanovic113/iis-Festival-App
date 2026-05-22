package com.festivalapp.prodaja.dto;

import com.festivalapp.prodaja.model.StageSegment;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StageSegmentResponse {

    private Long id;
    private Long segmentId;
    private String segmentName;
    private Integer capacity;

    public static StageSegmentResponse from(StageSegment ss) {
        StageSegmentResponse r = new StageSegmentResponse();
        r.id = ss.getId();
        r.segmentId = ss.getSegment().getSegmentId();
        r.segmentName = ss.getSegment().getName();
        r.capacity = ss.getCapacity();
        return r;
    }
}

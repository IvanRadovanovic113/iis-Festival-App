package com.festivalapp.prodaja.dto;

import com.festivalapp.prodaja.model.Segment;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SegmentResponse {

    private Long segmentId;
    private String naziv;
    private Long festivalId;
    private String festivalNaziv;

    public static SegmentResponse from(Segment s) {
        SegmentResponse r = new SegmentResponse();
        r.segmentId = s.getSegmentId();
        r.naziv = s.getNaziv();
        r.festivalId = s.getFestival().getFestivalId();
        r.festivalNaziv = s.getFestival().getNaziv();
        return r;
    }
}

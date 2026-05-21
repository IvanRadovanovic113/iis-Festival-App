package com.festivalapp.prodaja.dto;

import com.festivalapp.prodaja.model.BinaSegment;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BinaSegmentResponse {

    private Long id;
    private Long segmentId;
    private String segmentNaziv;
    private Integer kapacitet;

    public static BinaSegmentResponse from(BinaSegment bs) {
        BinaSegmentResponse r = new BinaSegmentResponse();
        r.id = bs.getId();
        r.segmentId = bs.getSegment().getSegmentId();
        r.segmentNaziv = bs.getSegment().getNaziv();
        r.kapacitet = bs.getKapacitet();
        return r;
    }
}

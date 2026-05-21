package com.festivalapp.dto;

import com.festivalapp.model.Bina;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BinaResponse {

    private Long binaId;
    private String naziv;
    private Integer kapacitet;
    private String lokacija;
    private Long festivalId;
    private String festivalNaziv;

    public static BinaResponse from(Bina bina) {
        BinaResponse r = new BinaResponse();
        r.binaId = bina.getBinaId();
        r.naziv = bina.getNaziv();
        r.kapacitet = bina.getKapacitet();
        r.lokacija = bina.getLokacija();
        r.festivalId = bina.getFestival().getFestivalId();
        r.festivalNaziv = bina.getFestival().getNaziv();
        return r;
    }
}

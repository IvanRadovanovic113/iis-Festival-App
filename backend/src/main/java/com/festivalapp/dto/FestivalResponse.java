package com.festivalapp.dto;

import com.festivalapp.model.Festival;
import com.festivalapp.model.FestivalStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class FestivalResponse {

    private Long festivalId;
    private String naziv;
    private String lokacija;
    private FestivalStatus status;
    private LocalDate datumPocetka;
    private LocalDate datumZavrsetka;

    public static FestivalResponse from(Festival festival) {
        FestivalResponse response = new FestivalResponse();
        response.festivalId = festival.getFestivalId();
        response.naziv = festival.getNaziv();
        response.lokacija = festival.getLokacija();
        response.status = festival.getStatus();
        response.datumPocetka = festival.getDatumPocetka();
        response.datumZavrsetka = festival.getDatumZavrsetka();
        return response;
    }
}

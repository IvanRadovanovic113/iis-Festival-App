package com.festivalapp.service;

import com.festivalapp.dto.FestivalRequest;
import com.festivalapp.dto.FestivalResponse;
import com.festivalapp.model.Festival;
import com.festivalapp.repository.FestivalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FestivalService {

    private final FestivalRepository festivalRepository;

    public FestivalResponse create(FestivalRequest request) {
        if (!request.getDatumZavrsetka().isAfter(request.getDatumPocetka())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Datum završetka mora biti posle datuma početka"
            );
        }
        Festival festival = Festival.builder()
            .naziv(request.getNaziv())
            .lokacija(request.getLokacija())
            .status(request.getStatus())
            .datumPocetka(request.getDatumPocetka())
            .datumZavrsetka(request.getDatumZavrsetka())
            .build();
        return FestivalResponse.from(festivalRepository.save(festival));
    }

    public List<FestivalResponse> getAll() {
        return festivalRepository.findAll().stream()
            .map(FestivalResponse::from)
            .toList();
    }

    public FestivalResponse getById(Long id) {
        return festivalRepository.findById(id)
            .map(FestivalResponse::from)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Festival nije pronađen"));
    }

    public void delete(Long id) {
        if (!festivalRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Festival nije pronađen");
        }
        festivalRepository.deleteById(id);
    }
}

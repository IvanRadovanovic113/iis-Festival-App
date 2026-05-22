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
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "End date must be after start date"
            );
        }
        Festival festival = Festival.builder()
            .name(request.getName())
            .location(request.getLocation())
            .status(request.getStatus())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
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
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Festival not found"));
    }

    public void delete(Long id) {
        if (!festivalRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Festival not found");
        }
        festivalRepository.deleteById(id);
    }
}

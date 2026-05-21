package com.festivalapp.prodaja.service;

import com.festivalapp.model.Bina;
import com.festivalapp.model.Festival;
import com.festivalapp.model.Role;
import com.festivalapp.model.User;
import com.festivalapp.model.UserFestivalAssignment;
import com.festivalapp.prodaja.dto.BinaSegmentRequest;
import com.festivalapp.prodaja.dto.BinaSegmentResponse;
import com.festivalapp.prodaja.dto.SegmentRequest;
import com.festivalapp.prodaja.dto.SegmentResponse;
import com.festivalapp.prodaja.model.BinaSegment;
import com.festivalapp.prodaja.model.Segment;
import com.festivalapp.prodaja.repository.BinaSegmentRepository;
import com.festivalapp.prodaja.repository.SegmentRepository;
import com.festivalapp.repository.BinaRepository;
import com.festivalapp.repository.UserFestivalAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SegmentService {

    private final SegmentRepository segmentRepository;
    private final BinaSegmentRepository binaSegmentRepository;
    private final BinaRepository binaRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;

    private Festival requireDirectorFestival(User user) {
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Nemate dodelu festivala"));
        if (assignment.getRole() != Role.DIREKTOR_PRODAJE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Samo direktor prodaje može upravljati segmentima");
        }
        return assignment.getFestival();
    }

    private void validateFestivalAccess(Festival userFestival, Long festivalId) {
        if (!userFestival.getFestivalId().equals(festivalId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nemate pristup ovom festivalu");
        }
    }

    public List<SegmentResponse> getFestivalSegments(Long festivalId, User user) {
        Festival festival = requireDirectorFestival(user);
        validateFestivalAccess(festival, festivalId);
        return segmentRepository.findByFestival_FestivalId(festivalId)
            .stream().map(SegmentResponse::from).toList();
    }

    public SegmentResponse createSegment(Long festivalId, SegmentRequest request, User user) {
        Festival festival = requireDirectorFestival(user);
        validateFestivalAccess(festival, festivalId);
        if (segmentRepository.existsByNazivAndFestival_FestivalId(request.getNaziv(), festivalId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Segment sa ovim nazivom već postoji na festivalu");
        }
        Segment segment = Segment.builder()
            .naziv(request.getNaziv())
            .festival(festival)
            .build();
        return SegmentResponse.from(segmentRepository.save(segment));
    }

    @Transactional
    public void deleteSegment(Long festivalId, Long segmentId, User user) {
        Festival festival = requireDirectorFestival(user);
        validateFestivalAccess(festival, festivalId);
        Segment segment = segmentRepository.findById(segmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Segment nije pronađen"));
        if (!segment.getFestival().getFestivalId().equals(festivalId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Segment ne pripada ovom festivalu");
        }
        binaSegmentRepository.deleteBySegment_SegmentId(segmentId);
        segmentRepository.deleteById(segmentId);
    }

    public List<BinaSegmentResponse> getStageSegments(Long stageId, User user) {
        Festival festival = requireDirectorFestival(user);
        Bina bina = binaRepository.findById(stageId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bina nije pronađena"));
        if (!bina.getFestival().getFestivalId().equals(festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bina ne pripada vašem festivalu");
        }
        return binaSegmentRepository.findByBina_BinaId(stageId)
            .stream().map(BinaSegmentResponse::from).toList();
    }

    public BinaSegmentResponse assignSegment(Long stageId, BinaSegmentRequest request, User user) {
        Festival festival = requireDirectorFestival(user);
        Bina bina = binaRepository.findById(stageId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bina nije pronađena"));
        if (!bina.getFestival().getFestivalId().equals(festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bina ne pripada vašem festivalu");
        }
        Segment segment = segmentRepository.findById(request.getSegmentId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Segment nije pronađen"));
        if (!segment.getFestival().getFestivalId().equals(festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Segment ne pripada vašem festivalu");
        }
        if (binaSegmentRepository.existsByBina_BinaIdAndSegment_SegmentId(stageId, request.getSegmentId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ovaj segment je već dodeljen ovoj bini");
        }
        BinaSegment binaSegment = BinaSegment.builder()
            .bina(bina)
            .segment(segment)
            .kapacitet(request.getKapacitet())
            .build();
        return BinaSegmentResponse.from(binaSegmentRepository.save(binaSegment));
    }

    public BinaSegmentResponse updateAssignment(Long stageId, Long segmentId, BinaSegmentRequest request, User user) {
        Festival festival = requireDirectorFestival(user);
        Bina bina = binaRepository.findById(stageId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bina nije pronađena"));
        if (!bina.getFestival().getFestivalId().equals(festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bina ne pripada vašem festivalu");
        }
        BinaSegment binaSegment = binaSegmentRepository
            .findByBina_BinaIdAndSegment_SegmentId(stageId, segmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dodela nije pronađena"));
        binaSegment.setKapacitet(request.getKapacitet());
        return BinaSegmentResponse.from(binaSegmentRepository.save(binaSegment));
    }

    @Transactional
    public void removeFromStage(Long stageId, Long segmentId, User user) {
        Festival festival = requireDirectorFestival(user);
        Bina bina = binaRepository.findById(stageId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bina nije pronađena"));
        if (!bina.getFestival().getFestivalId().equals(festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bina ne pripada vašem festivalu");
        }
        if (!binaSegmentRepository.existsByBina_BinaIdAndSegment_SegmentId(stageId, segmentId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dodela nije pronađena");
        }
        binaSegmentRepository.deleteByBina_BinaIdAndSegment_SegmentId(stageId, segmentId);
    }
}

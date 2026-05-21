package com.festivalapp.service;

import com.festivalapp.dto.BinaRequest;
import com.festivalapp.dto.BinaResponse;
import com.festivalapp.model.Bina;
import com.festivalapp.model.Festival;
import com.festivalapp.model.Role;
import com.festivalapp.model.User;
import com.festivalapp.model.UserFestivalAssignment;
import com.festivalapp.repository.BinaRepository;
import com.festivalapp.repository.UserFestivalAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BinaService {

    private final BinaRepository binaRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;

    private Festival requireAssignedFestival(User user) {
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Nemate dodelu festivala"));
        if (assignment.getRole() != Role.DIREKTOR_PRODAJE && assignment.getRole() != Role.EVENT_ORGANIZER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nemate pristup binama");
        }
        return assignment.getFestival();
    }

    private Festival requireDirectorFestival(User user) {
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Nemate dodelu festivala"));
        if (assignment.getRole() != Role.DIREKTOR_PRODAJE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Samo direktor prodaje može upravljati binama");
        }
        return assignment.getFestival();
    }

    public List<BinaResponse> getAll(User user) {
        Festival festival = requireAssignedFestival(user);
        return binaRepository.findByFestival_FestivalId(festival.getFestivalId())
            .stream().map(BinaResponse::from).toList();
    }

    public BinaResponse create(BinaRequest request, User user) {
        Festival festival = requireDirectorFestival(user);
        if (binaRepository.existsByNazivAndFestival_FestivalId(request.getNaziv(), festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bina sa ovim nazivom već postoji na ovom festivalu");
        }
        Bina bina = Bina.builder()
            .naziv(request.getNaziv())
            .kapacitet(request.getKapacitet())
            .lokacija(request.getLokacija())
            .festival(festival)
            .build();
        return BinaResponse.from(binaRepository.save(bina));
    }

    public BinaResponse update(Long id, BinaRequest request, User user) {
        Festival festival = requireDirectorFestival(user);
        Bina bina = binaRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bina nije pronađena"));
        if (!bina.getFestival().getFestivalId().equals(festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bina ne pripada vašem festivalu");
        }
        if (binaRepository.existsByNazivAndFestival_FestivalIdAndBinaIdNot(request.getNaziv(), festival.getFestivalId(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bina sa ovim nazivom već postoji na ovom festivalu");
        }
        bina.setNaziv(request.getNaziv());
        bina.setKapacitet(request.getKapacitet());
        bina.setLokacija(request.getLokacija());
        return BinaResponse.from(binaRepository.save(bina));
    }

    public void delete(Long id, User user) {
        Festival festival = requireDirectorFestival(user);
        Bina bina = binaRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bina nije pronađena"));
        if (!bina.getFestival().getFestivalId().equals(festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bina ne pripada vašem festivalu");
        }
        binaRepository.deleteById(id);
    }
}

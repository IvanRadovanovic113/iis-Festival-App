package com.festivalapp.service;

import com.festivalapp.dto.StageRequest;
import com.festivalapp.dto.StageResponse;
import com.festivalapp.model.Festival;
import com.festivalapp.model.Role;
import com.festivalapp.model.Stage;
import com.festivalapp.model.User;
import com.festivalapp.model.UserFestivalAssignment;
import com.festivalapp.repository.StageRepository;
import com.festivalapp.repository.UserFestivalAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StageService {

    private final StageRepository stageRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;

    private Festival requireSalesDirectorFestival(User user) {
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No festival assignment found"));
        if (assignment.getRole() != Role.SALES_DIRECTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the sales director can manage stages");
        }
        return assignment.getFestival();
    }

    public List<StageResponse> getAll(User user) {
        Festival festival = requireSalesDirectorFestival(user);
        return stageRepository.findByFestival_FestivalId(festival.getFestivalId())
            .stream().map(StageResponse::from).toList();
    }

    public StageResponse create(StageRequest request, User user) {
        Festival festival = requireSalesDirectorFestival(user);
        if (stageRepository.existsByNameAndFestival_FestivalId(request.getName(), festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A stage with this name already exists at this festival");
        }
        Stage stage = Stage.builder()
            .name(request.getName())
            .capacity(request.getCapacity())
            .location(request.getLocation())
            .festival(festival)
            .build();
        return StageResponse.from(stageRepository.save(stage));
    }

    public StageResponse update(Long id, StageRequest request, User user) {
        Festival festival = requireSalesDirectorFestival(user);
        Stage stage = stageRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stage not found"));
        if (!stage.getFestival().getFestivalId().equals(festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Stage does not belong to your festival");
        }
        if (stageRepository.existsByNameAndFestival_FestivalIdAndStageIdNot(request.getName(), festival.getFestivalId(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A stage with this name already exists at this festival");
        }
        stage.setName(request.getName());
        stage.setCapacity(request.getCapacity());
        stage.setLocation(request.getLocation());
        return StageResponse.from(stageRepository.save(stage));
    }

    public void delete(Long id, User user) {
        Festival festival = requireSalesDirectorFestival(user);
        Stage stage = stageRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stage not found"));
        if (!stage.getFestival().getFestivalId().equals(festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Stage does not belong to your festival");
        }
        stageRepository.deleteById(id);
    }
}

package com.festivalapp.prodaja.service;

import com.festivalapp.model.Festival;
import com.festivalapp.model.Role;
import com.festivalapp.model.Segment;
import com.festivalapp.model.Stage;
import com.festivalapp.model.User;
import com.festivalapp.model.UserFestivalAssignment;
import com.festivalapp.prodaja.dto.StageSegmentRequest;
import com.festivalapp.prodaja.dto.StageSegmentResponse;
import com.festivalapp.prodaja.dto.SegmentRequest;
import com.festivalapp.prodaja.dto.SegmentResponse;
import com.festivalapp.prodaja.model.StageSegment;
import com.festivalapp.prodaja.repository.StageSegmentRepository;
import com.festivalapp.prodaja.repository.SegmentRepository;
import com.festivalapp.repository.StageRepository;
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
    private final StageSegmentRepository stageSegmentRepository;
    private final StageRepository stageRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;

    private Festival requireSalesDirectorFestival(User user) {
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No festival assignment found"));
        if (assignment.getRole() != Role.SALES_DIRECTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the sales director can manage segments");
        }
        return assignment.getFestival();
    }

    private void validateFestivalAccess(Festival userFestival, Long festivalId) {
        if (!userFestival.getFestivalId().equals(festivalId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this festival");
        }
    }

    public List<SegmentResponse> getFestivalSegments(Long festivalId, User user) {
        Festival festival = requireSalesDirectorFestival(user);
        validateFestivalAccess(festival, festivalId);
        return segmentRepository.findByFestival_FestivalId(festivalId)
            .stream().map(SegmentResponse::from).toList();
    }

    public SegmentResponse createSegment(Long festivalId, SegmentRequest request, User user) {
        Festival festival = requireSalesDirectorFestival(user);
        validateFestivalAccess(festival, festivalId);
        if (segmentRepository.existsByNameAndFestival_FestivalId(request.getName(), festivalId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A segment with this name already exists at this festival");
        }
        Segment segment = Segment.builder()
            .name(request.getName())
            .festival(festival)
            .build();
        return SegmentResponse.from(segmentRepository.save(segment));
    }

    @Transactional
    public void deleteSegment(Long festivalId, Long segmentId, User user) {
        Festival festival = requireSalesDirectorFestival(user);
        validateFestivalAccess(festival, festivalId);
        Segment segment = segmentRepository.findById(segmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Segment not found"));
        if (!segment.getFestival().getFestivalId().equals(festivalId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Segment does not belong to this festival");
        }
        stageSegmentRepository.deleteBySegment_SegmentId(segmentId);
        segmentRepository.deleteById(segmentId);
    }

    public List<StageSegmentResponse> getStageSegments(Long stageId, User user) {
        Festival festival = requireSalesDirectorFestival(user);
        Stage stage = stageRepository.findById(stageId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stage not found"));
        if (!stage.getFestival().getFestivalId().equals(festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Stage does not belong to your festival");
        }
        return stageSegmentRepository.findByStage_StageId(stageId)
            .stream().map(StageSegmentResponse::from).toList();
    }

    public StageSegmentResponse assignSegment(Long stageId, StageSegmentRequest request, User user) {
        Festival festival = requireSalesDirectorFestival(user);
        Stage stage = stageRepository.findById(stageId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stage not found"));
        if (!stage.getFestival().getFestivalId().equals(festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Stage does not belong to your festival");
        }
        Segment segment = segmentRepository.findById(request.getSegmentId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Segment not found"));
        if (!segment.getFestival().getFestivalId().equals(festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Segment does not belong to your festival");
        }
        if (stageSegmentRepository.existsByStage_StageIdAndSegment_SegmentId(stageId, request.getSegmentId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This segment is already assigned to this stage");
        }
        StageSegment stageSegment = StageSegment.builder()
            .stage(stage)
            .segment(segment)
            .capacity(request.getCapacity())
            .build();
        return StageSegmentResponse.from(stageSegmentRepository.save(stageSegment));
    }

    public StageSegmentResponse updateAssignment(Long stageId, Long segmentId, StageSegmentRequest request, User user) {
        Festival festival = requireSalesDirectorFestival(user);
        Stage stage = stageRepository.findById(stageId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stage not found"));
        if (!stage.getFestival().getFestivalId().equals(festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Stage does not belong to your festival");
        }
        StageSegment stageSegment = stageSegmentRepository
            .findByStage_StageIdAndSegment_SegmentId(stageId, segmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));
        stageSegment.setCapacity(request.getCapacity());
        return StageSegmentResponse.from(stageSegmentRepository.save(stageSegment));
    }

    @Transactional
    public void removeFromStage(Long stageId, Long segmentId, User user) {
        Festival festival = requireSalesDirectorFestival(user);
        Stage stage = stageRepository.findById(stageId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stage not found"));
        if (!stage.getFestival().getFestivalId().equals(festival.getFestivalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Stage does not belong to your festival");
        }
        if (!stageSegmentRepository.existsByStage_StageIdAndSegment_SegmentId(stageId, segmentId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found");
        }
        stageSegmentRepository.deleteByStage_StageIdAndSegment_SegmentId(stageId, segmentId);
    }
}

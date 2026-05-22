package com.festivalapp.prodaja.controller;

import com.festivalapp.model.User;
import com.festivalapp.prodaja.dto.StageSegmentRequest;
import com.festivalapp.prodaja.dto.StageSegmentResponse;
import com.festivalapp.prodaja.dto.SegmentRequest;
import com.festivalapp.prodaja.dto.SegmentResponse;
import com.festivalapp.prodaja.service.SegmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SegmentController {

    private final SegmentService segmentService;

    @GetMapping("/api/festivals/{festivalId}/segments")
    public ResponseEntity<List<SegmentResponse>> getFestivalSegments(
            @PathVariable Long festivalId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(segmentService.getFestivalSegments(festivalId, user));
    }

    @PostMapping("/api/festivals/{festivalId}/segments")
    public ResponseEntity<SegmentResponse> createSegment(
            @PathVariable Long festivalId,
            @Valid @RequestBody SegmentRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(segmentService.createSegment(festivalId, request, user));
    }

    @DeleteMapping("/api/festivals/{festivalId}/segments/{segmentId}")
    public ResponseEntity<Void> deleteSegment(
            @PathVariable Long festivalId,
            @PathVariable Long segmentId,
            @AuthenticationPrincipal User user) {
        segmentService.deleteSegment(festivalId, segmentId, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/stages/{stageId}/segments")
    public ResponseEntity<List<StageSegmentResponse>> getStageSegments(
            @PathVariable Long stageId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(segmentService.getStageSegments(stageId, user));
    }

    @PostMapping("/api/stages/{stageId}/segments")
    public ResponseEntity<StageSegmentResponse> assignSegment(
            @PathVariable Long stageId,
            @Valid @RequestBody StageSegmentRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(segmentService.assignSegment(stageId, request, user));
    }

    @PutMapping("/api/stages/{stageId}/segments/{segmentId}")
    public ResponseEntity<StageSegmentResponse> updateAssignment(
            @PathVariable Long stageId,
            @PathVariable Long segmentId,
            @Valid @RequestBody StageSegmentRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(segmentService.updateAssignment(stageId, segmentId, request, user));
    }

    @DeleteMapping("/api/stages/{stageId}/segments/{segmentId}")
    public ResponseEntity<Void> removeFromStage(
            @PathVariable Long stageId,
            @PathVariable Long segmentId,
            @AuthenticationPrincipal User user) {
        segmentService.removeFromStage(stageId, segmentId, user);
        return ResponseEntity.noContent().build();
    }
}

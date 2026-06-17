package com.festivalapp.controller;

import com.festivalapp.dto.PerformerSchedulingResponse;
import com.festivalapp.dto.StageAssignmentRequest;
import com.festivalapp.model.User;
import com.festivalapp.service.PerformerSchedulingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/performer-scheduling")
@RequiredArgsConstructor
public class PerformerSchedulingController {

    private final PerformerSchedulingService schedulingService;

    @GetMapping
    public ResponseEntity<List<PerformerSchedulingResponse>> getAll(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(schedulingService.getAllScheduling(user));
    }

    @PutMapping("/{contractId}/stage")
    public ResponseEntity<PerformerSchedulingResponse> assignStage(
            @PathVariable Long contractId,
            @RequestBody StageAssignmentRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(schedulingService.assignStage(contractId, request.getStageId(), user));
    }

    @DeleteMapping("/{contractId}/stage")
    public ResponseEntity<Void> unassignStage(
            @PathVariable Long contractId,
            @AuthenticationPrincipal User user) {
        schedulingService.unassignStage(contractId, user);
        return ResponseEntity.noContent().build();
    }
}

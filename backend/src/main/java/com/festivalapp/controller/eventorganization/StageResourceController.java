package com.festivalapp.controller.eventorganization;

import com.festivalapp.dto.eventorganization.StageResourceRequest;
import com.festivalapp.dto.eventorganization.StageResourceResponse;
import com.festivalapp.model.User;
import com.festivalapp.service.eventorganization.StageResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/event-organization/stages/{stageId}/resources")
@RequiredArgsConstructor
public class StageResourceController {

    private final StageResourceService stageResourceService;

    @GetMapping
    public ResponseEntity<List<StageResourceResponse>> getStageResources(
            @PathVariable Long stageId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(stageResourceService.getStageResources(stageId, user));
    }

    @PostMapping
    public ResponseEntity<StageResourceResponse> assignResourceToStage(
            @PathVariable Long stageId,
            @Valid @RequestBody StageResourceRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(stageResourceService.assignResourceToStage(stageId, request, user));
    }

    @PutMapping("/{resourceId}")
    public ResponseEntity<StageResourceResponse> updateStageResource(
            @PathVariable Long stageId,
            @PathVariable Long resourceId,
            @Valid @RequestBody StageResourceRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(stageResourceService.updateStageResource(stageId, resourceId, request, user));
    }

    @DeleteMapping("/{resourceId}")
    public ResponseEntity<Void> removeResourceFromStage(
            @PathVariable Long stageId,
            @PathVariable Long resourceId,
            @AuthenticationPrincipal User user) {
        stageResourceService.removeResourceFromStage(stageId, resourceId, user);
        return ResponseEntity.noContent().build();
    }
}

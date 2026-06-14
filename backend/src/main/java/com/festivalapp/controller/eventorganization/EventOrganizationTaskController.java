package com.festivalapp.controller.eventorganization;

import com.festivalapp.dto.eventorganization.EventOrganizationTaskResponse;
import com.festivalapp.dto.eventorganization.RejectTaskRequest;
import com.festivalapp.dto.eventorganization.ResolveTaskRequest;
import com.festivalapp.model.User;
import com.festivalapp.model.eventorganization.EventOrganizationTaskStatus;
import com.festivalapp.service.eventorganization.EventOrganizationTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/event-organization/tasks")
@RequiredArgsConstructor
public class EventOrganizationTaskController {

    private final EventOrganizationTaskService taskService;

    @GetMapping
    public ResponseEntity<List<EventOrganizationTaskResponse>> getTasks(
            @RequestParam(required = false) EventOrganizationTaskStatus status,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(taskService.getTasks(status, user));
    }

    @PutMapping("/{taskId}/resolve")
    public ResponseEntity<EventOrganizationTaskResponse> resolveTask(
            @PathVariable Long taskId,
            @Valid @RequestBody ResolveTaskRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(taskService.resolveTask(taskId, request, user));
    }

    @PutMapping("/{taskId}/reject")
    public ResponseEntity<EventOrganizationTaskResponse> rejectTask(
            @PathVariable Long taskId,
            @Valid @RequestBody RejectTaskRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(taskService.rejectTask(taskId, request, user));
    }
}

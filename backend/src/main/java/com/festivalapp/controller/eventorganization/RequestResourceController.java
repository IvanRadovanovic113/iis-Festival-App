package com.festivalapp.controller.eventorganization;

import com.festivalapp.dto.eventorganization.RequestResourceRequest;
import com.festivalapp.dto.eventorganization.RequestResourceResponse;
import com.festivalapp.model.User;
import com.festivalapp.service.eventorganization.RequestResourceService;
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
@RequestMapping("/api/event-organization/requests/{requestId}/resources")
@RequiredArgsConstructor
public class RequestResourceController {

    private final RequestResourceService requestResourceService;

    @GetMapping
    public ResponseEntity<List<RequestResourceResponse>> getRequestResources(
            @PathVariable Long requestId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(requestResourceService.getRequestResources(requestId, user));
    }

    @PostMapping
    public ResponseEntity<RequestResourceResponse> addResourceToRequest(
            @PathVariable Long requestId,
            @Valid @RequestBody RequestResourceRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(requestResourceService.addResourceToRequest(requestId, request, user));
    }

    @PutMapping("/{resourceId}")
    public ResponseEntity<RequestResourceResponse> updateRequestResource(
            @PathVariable Long requestId,
            @PathVariable Long resourceId,
            @Valid @RequestBody RequestResourceRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(requestResourceService.updateRequestResource(requestId, resourceId, request, user));
    }

    @PutMapping("/{resourceId}/confirm")
    public ResponseEntity<RequestResourceResponse> confirmRequestResource(
            @PathVariable Long requestId,
            @PathVariable Long resourceId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(requestResourceService.confirmRequestResource(requestId, resourceId, user));
    }

    @DeleteMapping("/{resourceId}")
    public ResponseEntity<Void> removeResourceFromRequest(
            @PathVariable Long requestId,
            @PathVariable Long resourceId,
            @AuthenticationPrincipal User user) {
        requestResourceService.removeResourceFromRequest(requestId, resourceId, user);
        return ResponseEntity.noContent().build();
    }
}

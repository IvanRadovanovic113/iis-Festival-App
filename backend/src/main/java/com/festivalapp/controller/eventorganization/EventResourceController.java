package com.festivalapp.controller.eventorganization;

import com.festivalapp.dto.eventorganization.EventResourceRequest;
import com.festivalapp.dto.eventorganization.EventResourceResponse;
import com.festivalapp.model.User;
import com.festivalapp.service.eventorganization.EventResourceService;
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
@RequestMapping("/api/event-organization/resources")
@RequiredArgsConstructor
public class EventResourceController {

    private final EventResourceService eventResourceService;

    @GetMapping
    public ResponseEntity<List<EventResourceResponse>> getResources(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(eventResourceService.getResources(user));
    }

    @PostMapping
    public ResponseEntity<EventResourceResponse> createResource(
            @Valid @RequestBody EventResourceRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(eventResourceService.createResource(request, user));
    }

    @PutMapping("/{resourceId}")
    public ResponseEntity<EventResourceResponse> updateResource(
            @PathVariable Long resourceId,
            @Valid @RequestBody EventResourceRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(eventResourceService.updateResource(resourceId, request, user));
    }

    @DeleteMapping("/{resourceId}")
    public ResponseEntity<Void> deleteResource(
            @PathVariable Long resourceId,
            @AuthenticationPrincipal User user) {
        eventResourceService.deleteResource(resourceId, user);
        return ResponseEntity.noContent().build();
    }
}

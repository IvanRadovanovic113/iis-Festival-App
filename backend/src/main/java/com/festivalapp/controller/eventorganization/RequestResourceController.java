package com.festivalapp.controller.eventorganization;

import com.festivalapp.dto.eventorganization.RequestResourceResponse;
import com.festivalapp.model.User;
import com.festivalapp.service.eventorganization.RequestResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
}

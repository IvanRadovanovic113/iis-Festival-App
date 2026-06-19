package com.festivalapp.controller.eventorganization.analytics;

import com.festivalapp.dto.eventorganization.analytics.ResourceAnalyticsResponse;
import com.festivalapp.model.User;
import com.festivalapp.service.eventorganization.analytics.ResourceAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/event-organization/analytics")
@RequiredArgsConstructor
public class ResourceAnalyticsController {

    private final ResourceAnalyticsService analyticsService;

    @GetMapping
    public ResponseEntity<ResourceAnalyticsResponse> getAnalytics(
        @AuthenticationPrincipal User user,
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer month,
        @RequestParam(required = false) Long stageId
    ) {
        return ResponseEntity.ok(analyticsService.getAnalytics(user, year, month, stageId));
    }
}

package com.festivalapp.controller.eventorganization.analytics;

import com.festivalapp.dto.eventorganization.analytics.ResourceAnalyticsResponse;
import com.festivalapp.model.User;
import com.festivalapp.service.eventorganization.analytics.ResourceAnalyticsService;
import com.festivalapp.service.eventorganization.analytics.ResourceAnalyticsPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@RestController
@RequestMapping("/api/event-organization/analytics")
@RequiredArgsConstructor
public class ResourceAnalyticsController {

    private final ResourceAnalyticsService analyticsService;
    private final ResourceAnalyticsPdfService pdfService;

    @GetMapping
    public ResponseEntity<ResourceAnalyticsResponse> getAnalytics(
        @AuthenticationPrincipal User user,
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer month,
        @RequestParam(required = false) Long stageId
    ) {
        return ResponseEntity.ok(analyticsService.getAnalytics(user, year, month, stageId));
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> getPdf(
        @AuthenticationPrincipal User user,
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer month,
        @RequestParam(required = false) Long stageId,
        @RequestParam(required = false) String stageName
    ) {
        ResourceAnalyticsResponse analytics = analyticsService.getAnalytics(user, year, month, stageId);
        String periodLabel = buildPeriodLabel(year, month);
        byte[] pdf = pdfService.generate(analytics, periodLabel, stageName);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resource-analytics.pdf\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }

    private String buildPeriodLabel(Integer year, Integer month) {
        if (year == null) return "All time";
        if (month == null) return String.valueOf(year);
        String monthName = LocalDate.of(year, month, 1)
            .format(DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH));
        return monthName + " " + year;
    }
}

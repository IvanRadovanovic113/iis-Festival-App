package com.festivalapp.controller;

import com.festivalapp.dto.PerformerStatsDto;
import com.festivalapp.dto.StatePerformanceDTO;
import com.festivalapp.dto.NegotiationEfficiencyDTO;
import com.festivalapp.dto.AnalyticsTrendDTO;
import com.festivalapp.dto.OfferOutcomeDTO;
import com.festivalapp.dto.CriticalNegotiationDTO;
import com.festivalapp.model.PerformerType;
import com.festivalapp.service.NegotiationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/negotiation-manager/reports")
@RequiredArgsConstructor
public class ReportController {

    private final NegotiationService negotiationService;

    // 1. Izvestaj o uspesnosti izvodjaca
    @GetMapping("/performer-performance")
    public ResponseEntity<List<PerformerStatsDto>> getPerformerPerformanceReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) PerformerType type) {

        LocalDateTime start = (startDate != null) ? startDate : LocalDateTime.now().minusYears(1);
        LocalDateTime end = (endDate != null) ? endDate : LocalDateTime.now();

        List<PerformerStatsDto> report = negotiationService.getPerformerPerformanceReport(start, end, genre, type);
        return ResponseEntity.ok(report);
    }

    // 2. Izvestaj o uskim grlima
    @GetMapping("/bottlenecks")
    public ResponseEntity<List<StatePerformanceDTO>> getBottleneckReport(
            @RequestParam(required = false) Long templateId) {
        
        List<StatePerformanceDTO> report = negotiationService.getBottleneckReport(templateId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/efficiency")
    public ResponseEntity<NegotiationEfficiencyDTO> getNegotiationEfficiency(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(negotiationService.getNegotiationEfficiency(startDate, endDate));
    }

    @GetMapping("/negotiation-duration-trend")
    public ResponseEntity<List<AnalyticsTrendDTO>> getNegotiationDurationTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "YYYY-MM") String interval) {
        return ResponseEntity.ok(negotiationService.getNegotiationDurationTrend(startDate, endDate, interval));
    }

    @GetMapping("/offer-outcomes")
    public ResponseEntity<List<OfferOutcomeDTO>> getOfferOutcomes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(negotiationService.getOfferOutcomes(startDate, endDate));
    }

    @GetMapping("/offer-duration-trend")
    public ResponseEntity<List<AnalyticsTrendDTO>> getOfferDurationTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "YYYY-MM") String interval) {
        return ResponseEntity.ok(negotiationService.getOfferDurationTrend(startDate, endDate, interval));
    }

    @GetMapping("/critical-alerts")
    public ResponseEntity<List<CriticalNegotiationDTO>> getCriticalAlerts() {
        return ResponseEntity.ok(negotiationService.getCriticalNegotiations());
    }
}
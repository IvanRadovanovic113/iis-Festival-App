package com.festivalapp.prodaja.controller;

import com.festivalapp.model.User;
import com.festivalapp.prodaja.dto.*;
import com.festivalapp.prodaja.service.PdfReportService;
import com.festivalapp.prodaja.service.SalesAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SalesAnalyticsController {

    private final SalesAnalyticsService analyticsService;
    private final PdfReportService pdfReportService;

    @GetMapping("/api/festivals/{festivalId}/analytics/overview")
    public ResponseEntity<SalesOverviewResponse> getOverview(
            @PathVariable Long festivalId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(analyticsService.getOverview(festivalId, user));
    }

    @GetMapping("/api/festivals/{festivalId}/analytics/revenue-over-time")
    public ResponseEntity<List<RevenueDataPointDto>> getRevenueOverTime(
            @PathVariable Long festivalId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(analyticsService.getRevenueOverTime(festivalId, user));
    }

    @GetMapping("/api/festivals/{festivalId}/analytics/sales-by-ticket-type")
    public ResponseEntity<List<SalesByTicketTypeDto>> getSalesByTicketType(
            @PathVariable Long festivalId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(analyticsService.getSalesByTicketType(festivalId, user));
    }

    @GetMapping("/api/festivals/{festivalId}/analytics/promo-codes")
    public ResponseEntity<List<PromoCodeStatsDto>> getPromoCodeStats(
            @PathVariable Long festivalId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(analyticsService.getPromoCodeStats(festivalId, user));
    }

    @GetMapping("/api/festivals/{festivalId}/analytics/actions")
    public ResponseEntity<ActionsAnalyticsResponse> getActionsAnalytics(
            @PathVariable Long festivalId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(analyticsService.getActionsAnalytics(festivalId, user));
    }

    @GetMapping("/api/festivals/{festivalId}/analytics/dynamic-pricing")
    public ResponseEntity<DynamicPricingAnalyticsResponse> getDynamicPricingAnalytics(
            @PathVariable Long festivalId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(analyticsService.getDynamicPricingAnalytics(festivalId, user));
    }

    @GetMapping("/api/festivals/{festivalId}/analytics/pdf")
    public ResponseEntity<byte[]> generatePdfReport(
            @PathVariable Long festivalId,
            @AuthenticationPrincipal User user) throws Exception {
        SalesOverviewResponse overview = analyticsService.getOverview(festivalId, user);
        DynamicPricingAnalyticsResponse dynamicPricing = analyticsService.getDynamicPricingAnalytics(festivalId, user);

        Map<Long, List<TicketTypeSalesDataPointDto>> salesMap = new LinkedHashMap<>();
        Map<Long, List<TicketTypePriceHistoryDto>> priceMap = new LinkedHashMap<>();
        for (TicketTypeSummaryDto tt : overview.getTicketTypes()) {
            Long ttId = tt.getTicketTypeId();
            salesMap.put(ttId, analyticsService.getSalesOverTime(ttId, null, null, user));
            priceMap.put(ttId, analyticsService.getTicketTypePriceHistory(ttId, user));
        }

        byte[] pdf = pdfReportService.generateReport(overview, salesMap, priceMap, dynamicPricing);
        pdfReportService.saveToFile(pdf, overview.getFestivalName());

        String filename = "izvestaj-" + LocalDate.now() + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }

    @GetMapping("/api/ticket-types/{ticketTypeId}/analytics/sales-over-time")
    public ResponseEntity<List<TicketTypeSalesDataPointDto>> getSalesOverTime(
            @PathVariable Long ticketTypeId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(analyticsService.getSalesOverTime(ticketTypeId, from, to, user));
    }

    @GetMapping("/api/ticket-types/{ticketTypeId}/analytics/price-history")
    public ResponseEntity<List<TicketTypePriceHistoryDto>> getPriceHistory(
            @PathVariable Long ticketTypeId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(analyticsService.getTicketTypePriceHistory(ticketTypeId, user));
    }

    @GetMapping("/api/ticket-types/{ticketTypeId}/analytics/sales-by-period")
    public ResponseEntity<List<TicketTypeSalesByPeriodDto>> getSalesByPeriod(
            @PathVariable Long ticketTypeId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(analyticsService.getSalesByPeriod(ticketTypeId, user));
    }
}

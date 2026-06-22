package com.festivalapp.service.eventorganization.analytics;

import com.festivalapp.dto.eventorganization.analytics.ResourceAnalyticsResponse;
import com.festivalapp.dto.eventorganization.analytics.ResourceStageOccupancyResponse;
import com.festivalapp.dto.eventorganization.analytics.ResourceTopResourceResponse;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class ResourceAnalyticsPdfService {

    private static final Color[] BAR_COLORS = {
        new Color(0xcf, 0x4d, 0x7b),
        new Color(0x5b, 0xb8, 0xa0),
        new Color(0xf4, 0xa2, 0x59),
        new Color(0x7d, 0x5b, 0xa6),
        new Color(0x5b, 0x8e, 0xed)
    };

    private static final Color COLOR_TEXT  = new Color(43,  32,  48);
    private static final Color COLOR_SUB   = new Color(142, 128, 141);
    private static final Color COLOR_LIGHT = new Color(224, 208, 238);

    public byte[] generate(ResourceAnalyticsResponse analytics, String periodLabel, String stageName) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 40, 40, 50, 50);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titleFont    = new Font(Font.HELVETICA, 20, Font.BOLD,   COLOR_TEXT);
            Font subtitleFont = new Font(Font.HELVETICA, 11, Font.NORMAL, COLOR_SUB);
            Font sectionFont  = new Font(Font.HELVETICA, 13, Font.BOLD,   COLOR_TEXT);
            Font kpiLabel     = new Font(Font.HELVETICA,  9, Font.NORMAL, COLOR_SUB);
            Font kpiValue     = new Font(Font.HELVETICA, 14, Font.BOLD,   COLOR_TEXT);
            Font kpiSub       = new Font(Font.HELVETICA,  8, Font.NORMAL, COLOR_SUB);
            Font rowLabel     = new Font(Font.HELVETICA, 10, Font.NORMAL, COLOR_TEXT);
            Font rowCount     = new Font(Font.HELVETICA, 10, Font.BOLD,   COLOR_TEXT);
            Font footerFont   = new Font(Font.HELVETICA,  8, Font.NORMAL, COLOR_SUB);

            // Header
            Paragraph title = new Paragraph("Resource usage analytics", titleFont);
            title.setSpacingAfter(4);
            doc.add(title);

            String subText = stageName != null ? periodLabel + " · " + stageName : periodLabel;
            Paragraph sub = new Paragraph(subText, subtitleFont);
            sub.setSpacingAfter(4);
            doc.add(sub);

            String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH));
            Paragraph gen = new Paragraph("Generated on " + dateStr, footerFont);
            gen.setSpacingAfter(24);
            doc.add(gen);

            // Kartice gornje
            PdfPTable kpiTable = new PdfPTable(4);
            kpiTable.setWidthPercentage(100);
            kpiTable.setSpacingAfter(28);

            String resourceName = analytics.getMostUsedResourceName() != null
                ? analytics.getMostUsedResourceName() : "-";

            addKpiCard(kpiTable, "Total reservations",
                String.valueOf(analytics.getTotalReservations()), periodLabel,
                new Color(255, 255, 255), new Color(237, 224, 236),
                kpiLabel, kpiValue, kpiSub);

            addKpiCard(kpiTable, "Most used resource",
                resourceName, analytics.getMostUsedResourceCount() + " requests",
                new Color(237, 250, 244), new Color(184, 232, 212),
                kpiLabel, kpiValue, kpiSub);

            addKpiCard(kpiTable, "Stage occupancy",
                String.format("%.1f%%", analytics.getAvgStageOccupancy()), "avg all stages",
                new Color(255, 245, 236), new Color(253, 214, 181),
                kpiLabel, kpiValue, kpiSub);

            addKpiCard(kpiTable, "Extra resource requests",
                String.valueOf(analytics.getExtraResourceRequests()), "non-existing",
                new Color(255, 245, 249), new Color(245, 200, 216),
                kpiLabel, kpiValue, kpiSub);

            doc.add(kpiTable);

            // Zauzetost bine
            Paragraph stageSection = new Paragraph("Stage occupancy", sectionFont);
            stageSection.setSpacingAfter(4);
            doc.add(stageSection);

            Paragraph stageSub = new Paragraph(periodLabel, subtitleFont);
            stageSub.setSpacingAfter(12);
            doc.add(stageSub);

            List<ResourceStageOccupancyResponse> stages = analytics.getStageOccupancies();
            if (stages == null || stages.isEmpty()) {
                Paragraph empty = new Paragraph("No reservations found for this period.", subtitleFont);
                empty.setSpacingAfter(24);
                doc.add(empty);
            } else {
                PdfPTable stageTable = new PdfPTable(3);
                stageTable.setWidths(new float[]{3f, 6.5f, 1f});
                stageTable.setWidthPercentage(100);
                stageTable.setSpacingAfter(28);

                for (int i = 0; i < stages.size(); i++) {
                    ResourceStageOccupancyResponse stage = stages.get(i);
                    double pct = Math.min(Math.max(stage.getOccupancyPercent(), 0), 100);
                    addBarRow(stageTable, stage.getStageName(),
                        String.format("%.0f%%", pct), pct,
                        BAR_COLORS[i % BAR_COLORS.length], rowLabel, rowCount);
                }
                doc.add(stageTable);
            }

            // Top resursi
            Paragraph resourceSection = new Paragraph("Top resources", sectionFont);
            resourceSection.setSpacingAfter(4);
            doc.add(resourceSection);

            Paragraph resourceSub = new Paragraph("Most requested", subtitleFont);
            resourceSub.setSpacingAfter(12);
            doc.add(resourceSub);

            List<ResourceTopResourceResponse> topResources = analytics.getTopResources();
            if (topResources == null || topResources.isEmpty()) {
                doc.add(new Paragraph("No resource requests found for this period.", subtitleFont));
            } else {
                long maxCount = topResources.stream()
                    .mapToLong(ResourceTopResourceResponse::getRequestCount)
                    .max().orElse(1);

                PdfPTable resourceTable = new PdfPTable(3);
                resourceTable.setWidths(new float[]{3f, 6.5f, 1f});
                resourceTable.setWidthPercentage(100);

                for (int i = 0; i < topResources.size(); i++) {
                    ResourceTopResourceResponse r = topResources.get(i);
                    double pct = (double) r.getRequestCount() / maxCount * 100;
                    addBarRow(resourceTable, r.getResourceName(),
                        r.getRequestCount() + "x", pct,
                        BAR_COLORS[i % BAR_COLORS.length], rowLabel, rowCount);
                }
                doc.add(resourceTable);
            }

            doc.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate analytics PDF", e);
        }
    }

    private void addKpiCard(PdfPTable table, String label, String value, String subText,
                             Color bg, Color border,
                             Font labelFont, Font valueFont, Font subFont) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(12);
        cell.setBorderColor(border);
        cell.setBorderWidth(1);
        cell.setBackgroundColor(bg);

        cell.addElement(new Paragraph(label, labelFont));
        cell.addElement(new Paragraph(value, valueFont));
        cell.addElement(new Paragraph(subText, subFont));
        table.addCell(cell);
    }

    private void addBarRow(PdfPTable table, String name, String countLabel, double percent,
                            Color barColor, Font labelFont, Font countFont) {
        PdfPCell nameCell = new PdfPCell(new Phrase(name, labelFont));
        nameCell.setBorder(Rectangle.NO_BORDER);
        nameCell.setPaddingBottom(10);
        nameCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(nameCell);

        float filled = (float) Math.max(percent, 2.0);
        float empty  = Math.max(100f - filled, 1f);

        PdfPTable barTable = new PdfPTable(2);
        try {
            barTable.setWidths(new float[]{filled, empty});
        } catch (DocumentException ignored) {}

        PdfPCell filledCell = new PdfPCell();
        filledCell.setFixedHeight(12);
        filledCell.setBackgroundColor(barColor);
        filledCell.setBorder(Rectangle.NO_BORDER);
        barTable.addCell(filledCell);

        PdfPCell emptyCell = new PdfPCell();
        emptyCell.setFixedHeight(12);
        emptyCell.setBackgroundColor(COLOR_LIGHT);
        emptyCell.setBorder(Rectangle.NO_BORDER);
        barTable.addCell(emptyCell);

        PdfPCell barCell = new PdfPCell(barTable);
        barCell.setBorder(Rectangle.NO_BORDER);
        barCell.setPaddingBottom(10);
        barCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(barCell);

        PdfPCell countCell = new PdfPCell(new Phrase(countLabel, countFont));
        countCell.setBorder(Rectangle.NO_BORDER);
        countCell.setPaddingLeft(8);
        countCell.setPaddingBottom(10);
        countCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(countCell);
    }
}

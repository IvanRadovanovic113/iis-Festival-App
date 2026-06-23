package com.festivalapp.prodaja.service;

import com.festivalapp.prodaja.dto.*;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PdfReportService {

    @Value("${app.pdf.output-dir}")
    private String outputDir;

    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter FILE_DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm");

    public byte[] generateReport(
            SalesOverviewResponse overview,
            Map<Long, List<TicketTypeSalesDataPointDto>> salesMap,
            Map<Long, List<TicketTypePriceHistoryDto>> priceMap,
            DynamicPricingAnalyticsResponse dynamicPricing) throws Exception {

        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, baos);
        doc.open();

        Font titleFont     = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font sectionFont   = new Font(Font.HELVETICA, 13, Font.BOLD);
        Font subFont       = new Font(Font.HELVETICA, 11, Font.BOLD);
        Font normalFont    = new Font(Font.HELVETICA, 10);
        Font headerCellFont = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
        Font cellFont      = new Font(Font.HELVETICA, 9);

        // Zaglavlje
        doc.add(new Paragraph("Izvestaj o prodaji - " + overview.getFestivalName(), titleFont));
        doc.add(new Paragraph("Generisano: " + LocalDateTime.now().format(DATE_TIME_FMT), normalFont));
        doc.add(new Paragraph(" "));

        // Pregled prodaje
        doc.add(new Paragraph("Pregled prodaje", sectionFont));
        doc.add(new Paragraph("Ukupan prihod: " + formatCurrency(overview.getTotalRevenue()), normalFont));
        doc.add(new Paragraph("Prodato karata: " + overview.getTotalTicketsSold() + " / " + overview.getTotalTicketsAvailable(), normalFont));
        doc.add(new Paragraph("Aktivnih tipova karata: " + overview.getActiveTicketTypes(), normalFont));
        doc.add(new Paragraph(" "));

        // Tabela tipova karata
        doc.add(new Paragraph("Tipovi karata", sectionFont));
        PdfPTable ttTable = new PdfPTable(new float[]{3, 1.5f, 1.5f, 2});
        ttTable.setWidthPercentage(100);
        addHeaderCell(ttTable, "Naziv", headerCellFont);
        addHeaderCell(ttTable, "Prodato", headerCellFont);
        addHeaderCell(ttTable, "Ukupno", headerCellFont);
        addHeaderCell(ttTable, "Prihod", headerCellFont);
        for (TicketTypeSummaryDto tt : overview.getTicketTypes()) {
            addCell(ttTable, tt.getName(), cellFont, false);
            addCell(ttTable, String.valueOf(tt.getSoldCount()), cellFont, true);
            addCell(ttTable, String.valueOf(tt.getTotalQuantity()), cellFont, true);
            addCell(ttTable, formatCurrency(tt.getRevenue()), cellFont, true);
        }
        doc.add(ttTable);

        // Grafici po tipu karte
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("Analitika po tipu karte", sectionFont));

        for (TicketTypeSummaryDto tt : overview.getTicketTypes()) {
            Long id = tt.getTicketTypeId();
            List<TicketTypeSalesDataPointDto> sales = salesMap.getOrDefault(id, List.of());
            List<TicketTypePriceHistoryDto> prices = priceMap.getOrDefault(id, List.of());

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph(tt.getName(), subFont));

            if (!sales.isEmpty()) {
                JFreeChart salesChart = buildSalesOverTimeChart(sales);
                Image salesImg = chartToImage(salesChart, 500, 220);
                salesImg.setAlignment(Element.ALIGN_CENTER);
                doc.add(salesImg);
            } else {
                doc.add(new Paragraph("Nema podataka o prodaji.", normalFont));
            }

            doc.add(new Paragraph(" "));

            if (!prices.isEmpty()) {
                JFreeChart priceChart = buildPriceHistoryChart(prices);
                Image priceImg = chartToImage(priceChart, 500, 220);
                priceImg.setAlignment(Element.ALIGN_CENTER);
                doc.add(priceImg);
            } else {
                doc.add(new Paragraph("Nema zabeleZenih promena cene.", normalFont));
            }
        }

        // Dynamic pricing
        if (dynamicPricing != null && !dynamicPricing.getTicketTypes().isEmpty()) {
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Dynamic Pricing Impact", sectionFont));
            doc.add(new Paragraph("Ukupan revenue premium: " + formatCurrency(dynamicPricing.getTotalRevenuePremium()), normalFont));
            doc.add(new Paragraph("Ukupan baseline prihod: " + formatCurrency(dynamicPricing.getTotalBaselineRevenue()), normalFont));
            doc.add(new Paragraph(" "));

            PdfPTable dpTable = new PdfPTable(new float[]{3, 1.2f, 1.5f, 2, 2, 1.5f});
            dpTable.setWidthPercentage(100);
            addHeaderCell(dpTable, "Tip karte", headerCellFont);
            addHeaderCell(dpTable, "DP aktivan", headerCellFont);
            addHeaderCell(dpTable, "Prodato", headerCellFont);
            addHeaderCell(dpTable, "Baseline prihod", headerCellFont);
            addHeaderCell(dpTable, "Revenue premium", headerCellFont);
            addHeaderCell(dpTable, "Premium %", headerCellFont);
            for (DynamicPricingTicketTypeDto dp : dynamicPricing.getTicketTypes()) {
                addCell(dpTable, dp.getName(), cellFont, false);
                addCell(dpTable, Boolean.TRUE.equals(dp.getDynamicPricingActive()) ? "Da" : "Ne", cellFont, true);
                addCell(dpTable, String.valueOf(dp.getTotalTicketsSold()), cellFont, true);
                addCell(dpTable, formatCurrency(dp.getBaselineRevenue()), cellFont, true);
                addCell(dpTable, formatCurrency(dp.getRevenuePremium()), cellFont, true);
                String pct = dp.getBaselineRevenue() != null && dp.getBaselineRevenue().compareTo(BigDecimal.ZERO) > 0
                        ? dp.getRevenuePremium()
                                .divide(dp.getBaselineRevenue(), 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .setScale(1, RoundingMode.HALF_UP) + "%"
                        : "-";
                addCell(dpTable, pct, cellFont, true);
            }
            doc.add(dpTable);
        }

        doc.close();
        return baos.toByteArray();
    }

    public String saveToFile(byte[] content, String festivalName) {
        try {
            Path dir = Paths.get(outputDir);
            Files.createDirectories(dir);
            String sanitized = festivalName.replaceAll("[^a-zA-Z0-9]", "-").replaceAll("-+", "-");
            String filename = sanitized + "-" + LocalDateTime.now().format(FILE_DATE_FMT) + ".pdf";
            Path file = dir.resolve(filename);
            Files.write(file, content);
            log.info("PDF izvestaj sacuvan: {}", file.toAbsolutePath());
            return file.toAbsolutePath().toString();
        } catch (IOException e) {
            log.error("Greska pri cuvanju PDF-a: {}", e.getMessage());
            return null;
        }
    }

    private JFreeChart buildSalesOverTimeChart(List<TicketTypeSalesDataPointDto> data) {
        TimeSeries series = new TimeSeries("Prodato karata");
        for (TicketTypeSalesDataPointDto dp : data) {
            LocalDate date = LocalDate.parse(dp.getDate());
            series.addOrUpdate(
                    new Day(date.getDayOfMonth(), date.getMonthValue(), date.getYear()),
                    dp.getTicketsSold()
            );
        }
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Prodaja po danu", "Datum", "Broj karata",
                new TimeSeriesCollection(series), false, false, false);
        chart.setBackgroundPaint(Color.WHITE);
        chart.getXYPlot().setBackgroundPaint(Color.WHITE);
        return chart;
    }

    private JFreeChart buildPriceHistoryChart(List<TicketTypePriceHistoryDto> data) {
        TimeSeries series = new TimeSeries("Cena (EUR)");
        for (TicketTypePriceHistoryDto p : data) {
            if (p.getDatum() == null) continue;
            LocalDateTime ldt = LocalDateTime.parse(p.getDatum());
            series.addOrUpdate(
                    new Minute(ldt.getMinute(), ldt.getHour(),
                               ldt.getDayOfMonth(), ldt.getMonthValue(), ldt.getYear()),
                    p.getNewPrice()
            );
        }
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Istorija cene", "Datum", "Cena (EUR)",
                new TimeSeriesCollection(series), false, false, false);
        chart.setBackgroundPaint(Color.WHITE);
        chart.getXYPlot().setBackgroundPaint(Color.WHITE);
        return chart;
    }

    private Image chartToImage(JFreeChart chart, int width, int height) throws Exception {
        BufferedImage img = chart.createBufferedImage(width, height);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return Image.getInstance(baos.toByteArray());
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(79, 70, 229));
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text, Font font, boolean center) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "-", font));
        cell.setPadding(5);
        if (center) {
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        }
        table.addCell(cell);
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) return "-";
        return "EUR " + value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}

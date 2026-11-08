package com.Clinivora.util;

import com.Clinivora.model.CaseReportRow;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class PdfReportExporter {
    private PdfReportExporter() {
    }

    public static void export(Path file,
                              String summary,
                              Map<String, Long> statusCounts,
                              List<CaseReportRow> rows) throws IOException {
        try (OutputStream out = Files.newOutputStream(file)) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            document.add(new Paragraph("Clinivora Operations Report", titleFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(summary == null ? "" : summary, bodyFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Case status totals", headingFont));
            PdfPTable statusTable = new PdfPTable(2);
            statusTable.setWidthPercentage(60);
            statusTable.addCell(header("Status"));
            statusTable.addCell(header("Count"));
            for (Map.Entry<String, Long> entry : statusCounts.entrySet()) {
                statusTable.addCell(cell(entry.getKey()));
                statusTable.addCell(cell(String.valueOf(entry.getValue())));
            }
            document.add(statusTable);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Case details", headingFont));
            PdfPTable detail = new PdfPTable(7);
            detail.setWidthPercentage(100);
            detail.setWidths(new float[]{1.2f, 1.1f, 1.4f, 1.1f, 1.4f, 1.6f, 0.8f});
            detail.addCell(header("Case"));
            detail.addCell(header("Patient"));
            detail.addCell(header("Name"));
            detail.addCell(header("Status"));
            detail.addCell(header("Arrival"));
            detail.addCell(header("Complaint"));
            detail.addCell(header("ESI"));
            for (CaseReportRow row : rows) {
                detail.addCell(cell(row.getCaseCode()));
                detail.addCell(cell(row.getPatientCode()));
                detail.addCell(cell(row.getPatientName()));
                detail.addCell(cell(row.getStatus() == null ? "" : row.getStatus().name()));
                detail.addCell(cell(DateTimeUtil.formatDisplay(row.getArrivalTime())));
                detail.addCell(cell(row.getChiefComplaint() == null ? "" : row.getChiefComplaint()));
                detail.addCell(cell(row.getPriority() == null ? "" : row.getPriority().name()));
            }
            document.add(detail);
            document.close();
        } catch (DocumentException e) {
            throw new IOException("Failed to write PDF", e);
        }
    }

    private static PdfPCell header(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9)));
        cell.setBackgroundColor(new Color(232, 240, 254));
        cell.setPadding(4);
        return cell;
    }

    private static PdfPCell cell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text,
                FontFactory.getFont(FontFactory.HELVETICA, 8)));
        cell.setPadding(3);
        return cell;
    }
}

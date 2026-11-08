package com.Clinivora.service;

import com.Clinivora.model.CaseReportRow;
import com.Clinivora.model.EmergencyCase;
import com.Clinivora.model.Patient;
import com.Clinivora.model.enums.CaseStatus;
import com.Clinivora.model.enums.CaseType;
import com.Clinivora.model.enums.TriagePriority;
import com.Clinivora.util.CsvReportExporter;
import com.Clinivora.util.PdfReportExporter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReportServiceTest extends ServiceTestSupport {

    @TempDir
    Path exportDir;

    @BeforeEach
    void setUp() {
        boot();
    }

    @AfterEach
    void tearDown() {
        shutdown();
    }

    @Test
    void buildsDetailedReportAndExports() throws Exception {
        loginReception();
        Patient patient = context.getPatientService().register(
                "Report", "Patient", null, null, null, null, null, null);
        EmergencyCase emergencyCase = context.getEmergencyCaseService()
                .openCase(patient.getId(), "Headache", CaseType.EMERGENCY);
        logout();

        loginTriage();
        context.getTriageService().assess(emergencyCase.getId(), TriagePriority.P3, "BP normal", "stable");
        logout();

        loginAdmin();
        LocalDate today = LocalDate.now();
        ReportService reports = context.getReportService();
        List<CaseReportRow> rows = reports.listDetailedCases(today.minusDays(1), today.plusDays(1), null);
        assertFalse(rows.isEmpty());
        assertTrue(rows.stream().anyMatch(r -> emergencyCase.getDisplayCode().equals(r.getCaseCode())));

        Map<String, Long> daily = reports.dailyArrivalCounts(today.minusDays(1), today.plusDays(1));
        assertTrue(daily.values().stream().mapToLong(Long::longValue).sum() >= 1);

        Map<String, Long> status = reports.caseStatusReport();
        assertTrue(status.getOrDefault(CaseStatus.TRIAGED.name(), 0L) >= 1
                || status.values().stream().mapToLong(Long::longValue).sum() >= 1);

        String summary = reports.buildSummaryText(
                today.minusDays(1), today.plusDays(1), null, rows, status, daily, Map.of());
        assertTrue(summary.contains("Clinivora Operations Report"));

        Path csv = exportDir.resolve("report.csv");
        CsvReportExporter.export(csv, rows);
        assertTrue(Files.size(csv) > 0);
        assertTrue(Files.readString(csv).contains(emergencyCase.getDisplayCode()));

        Path pdf = exportDir.resolve("report.pdf");
        PdfReportExporter.export(pdf, summary, status, rows);
        assertTrue(Files.size(pdf) > 100);
    }
}

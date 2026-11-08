package com.Clinivora.service;

import com.Clinivora.dsa.HashTable;
import com.Clinivora.model.CaseReportRow;
import com.Clinivora.model.EmergencyCase;
import com.Clinivora.model.enums.CaseStatus;
import com.Clinivora.model.enums.TriagePriority;
import com.Clinivora.repository.CaseAssignmentRepository;
import com.Clinivora.repository.EmergencyCaseRepository;
import com.Clinivora.repository.TriageAssessmentRepository;
import com.Clinivora.util.DateTimeUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportService {
    private final EmergencyCaseRepository caseRepository;
    private final TriageAssessmentRepository triageRepository;
    private final CaseAssignmentRepository assignmentRepository;

    public ReportService(EmergencyCaseRepository caseRepository,
                         TriageAssessmentRepository triageRepository,
                         CaseAssignmentRepository assignmentRepository) {
        this.caseRepository = caseRepository;
        this.triageRepository = triageRepository;
        this.assignmentRepository = assignmentRepository;
    }

    public Map<String, Long> caseStatusReport() {
        try {
            Map<String, Long> report = new LinkedHashMap<>();
            for (CaseStatus status : CaseStatus.values()) {
                report.put(status.name(), caseRepository.countByStatus(status));
            }
            return report;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to build status report", e);
        }
    }

    public Map<String, Long> triagePriorityReport() {
        try {
            HashTable<String, Long> counts = new HashTable<>();
            for (TriagePriority priority : TriagePriority.values()) {
                counts.put(priority.name(), 0L);
            }
            List<EmergencyCase> open = caseRepository.findAllOpen();
            for (EmergencyCase emergencyCase : open) {
                var assessment = triageRepository.findByCaseId(emergencyCase.getId());
                if (assessment.isPresent()) {
                    String key = assessment.get().getPriority().name();
                    Long current = counts.get(key);
                    counts.put(key, current == null ? 1L : current + 1L);
                }
            }
            Map<String, Long> report = new LinkedHashMap<>();
            for (TriagePriority priority : TriagePriority.values()) {
                Long value = counts.get(priority.name());
                report.put(priority.name(), value == null ? 0L : value);
            }
            return report;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to build triage report", e);
        }
    }

    public List<CaseReportRow> listDetailedCases(LocalDate start, LocalDate end, CaseStatus statusOrNull) {
        requireRange(start, end);
        try {
            return caseRepository.findReportRowsBetween(startBound(start), endBound(end), statusOrNull);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list detailed cases", e);
        }
    }

    public Map<String, Long> dailyArrivalCounts(LocalDate start, LocalDate end) {
        List<CaseReportRow> rows = listDetailedCases(start, end, null);
        Map<String, Long> counts = new LinkedHashMap<>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            counts.put(cursor.toString(), 0L);
            cursor = cursor.plusDays(1);
        }
        for (CaseReportRow row : rows) {
            if (row.getArrivalTime() == null) {
                continue;
            }
            String day = row.getArrivalTime().toLocalDate().toString();
            counts.put(day, counts.getOrDefault(day, 0L) + 1L);
        }
        return counts;
    }

    public Map<String, Long> doctorAssignmentCounts(LocalDate start, LocalDate end) {
        requireRange(start, end);
        try {
            return assignmentRepository.countAssignmentsByDoctor(startBound(start), endBound(end));
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to count doctor assignments", e);
        }
    }

    public String buildSummaryText(LocalDate start,
                                   LocalDate end,
                                   CaseStatus statusOrNull,
                                   List<CaseReportRow> rows,
                                   Map<String, Long> statusCounts,
                                   Map<String, Long> dailyCounts,
                                   Map<String, Long> doctorCounts) {
        StringBuilder sb = new StringBuilder();
        sb.append("Clinivora Operations Report\n");
        sb.append("Period: ").append(start).append(" → ").append(end).append('\n');
        sb.append("Status filter: ").append(statusOrNull == null ? "ALL" : statusOrNull.name()).append('\n');
        sb.append("Cases in detail table: ").append(rows.size()).append('\n');
        long peak = 0;
        String peakDay = "-";
        for (Map.Entry<String, Long> entry : dailyCounts.entrySet()) {
            if (entry.getValue() > peak) {
                peak = entry.getValue();
                peakDay = entry.getKey();
            }
        }
        sb.append("Peak arrival day: ").append(peakDay).append(" (").append(peak).append(")\n");
        sb.append("Status totals (system-wide): ");
        statusCounts.forEach((k, v) -> sb.append(k).append('=').append(v).append(' '));
        sb.append('\n');
        if (doctorCounts.isEmpty()) {
            sb.append("Doctor assignments in range: none\n");
        } else {
            sb.append("Doctor assignments in range:\n");
            doctorCounts.forEach((name, count) ->
                    sb.append("  • ").append(name).append(": ").append(count).append('\n'));
        }
        return sb.toString().trim();
    }

    private static void requireRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end dates are required");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End date must be on or after start date");
        }
    }

    private static String startBound(LocalDate date) {
        return DateTimeUtil.formatDateTime(LocalDateTime.of(date, LocalTime.MIN));
    }

    private static String endBound(LocalDate date) {
        return DateTimeUtil.formatDateTime(LocalDateTime.of(date, LocalTime.of(23, 59, 59)));
    }
}

package com.Clinivora.util;

import com.Clinivora.model.CaseReportRow;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class CsvReportExporter {
    private CsvReportExporter() {
    }

    public static void export(Path file, List<CaseReportRow> rows) throws IOException {
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write("case_code,patient_code,patient_name,status,arrival,complaint,priority\n");
            for (CaseReportRow row : rows) {
                writer.write(escape(row.getCaseCode()));
                writer.write(',');
                writer.write(escape(row.getPatientCode()));
                writer.write(',');
                writer.write(escape(row.getPatientName()));
                writer.write(',');
                writer.write(escape(row.getStatus() == null ? "" : row.getStatus().name()));
                writer.write(',');
                writer.write(escape(DateTimeUtil.formatDateTime(row.getArrivalTime())));
                writer.write(',');
                writer.write(escape(row.getChiefComplaint()));
                writer.write(',');
                writer.write(escape(row.getPriority() == null ? "" : row.getPriority().name()));
                writer.write('\n');
            }
        }
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}

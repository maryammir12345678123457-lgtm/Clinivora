package com.Clinivora.repository;

import com.Clinivora.model.EmergencyCase;
import com.Clinivora.model.enums.CaseStatus;
import com.Clinivora.model.enums.CaseType;
import com.Clinivora.util.DateTimeUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmergencyCaseRepository {

    public Optional<EmergencyCase> findById(long id) throws SQLException {
        String sql = """
                SELECT id, display_code, patient_id, chief_complaint, arrival_time, status, case_type,
                       created_by, created_at, updated_at
                FROM emergency_case WHERE id = ?
                """;
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<EmergencyCase> findByStatus(CaseStatus status) throws SQLException {
        String sql = """
                SELECT id, display_code, patient_id, chief_complaint, arrival_time, status, case_type,
                       created_by, created_at, updated_at
                FROM emergency_case WHERE status = ? ORDER BY arrival_time
                """;
        List<EmergencyCase> cases = new ArrayList<>();
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cases.add(map(rs));
                }
            }
        }
        return cases;
    }

    public List<EmergencyCase> findByTypeAndStatuses(CaseType type, CaseStatus... statuses) throws SQLException {
        if (statuses == null || statuses.length == 0) {
            return List.of();
        }
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < statuses.length; i++) {
            if (i > 0) {
                placeholders.append(',');
            }
            placeholders.append('?');
        }
        String sql = """
                SELECT id, display_code, patient_id, chief_complaint, arrival_time, status, case_type,
                       created_by, created_at, updated_at
                FROM emergency_case
                WHERE case_type = ? AND status IN (%s)
                ORDER BY arrival_time
                """.formatted(placeholders);
        List<EmergencyCase> cases = new ArrayList<>();
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setString(1, type.name());
            for (int i = 0; i < statuses.length; i++) {
                ps.setString(i + 2, statuses[i].name());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cases.add(map(rs));
                }
            }
        }
        return cases;
    }

    public List<EmergencyCase> findAllOpen() throws SQLException {
        String sql = """
                SELECT id, display_code, patient_id, chief_complaint, arrival_time, status, case_type,
                       created_by, created_at, updated_at
                FROM emergency_case
                WHERE status NOT IN ('DISCHARGED', 'CANCELLED')
                ORDER BY arrival_time
                """;
        List<EmergencyCase> cases = new ArrayList<>();
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cases.add(map(rs));
            }
        }
        return cases;
    }

    public long insert(EmergencyCase emergencyCase) throws SQLException {
        String sql = """
                INSERT INTO emergency_case(display_code, patient_id, chief_complaint, arrival_time, status,
                                           case_type, created_by, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql, JdbcSupport.RETURN_GENERATED)) {
            ps.setString(1, emergencyCase.getDisplayCode());
            ps.setLong(2, emergencyCase.getPatientId());
            JdbcSupport.setNullableString(ps, 3, emergencyCase.getChiefComplaint());
            ps.setString(4, DateTimeUtil.formatDateTime(emergencyCase.getArrivalTime()));
            ps.setString(5, emergencyCase.getStatus().name());
            ps.setString(6, emergencyCase.getCaseType().name());
            JdbcSupport.setNullableLong(ps, 7, emergencyCase.getCreatedBy());
            ps.setString(8, DateTimeUtil.formatDateTime(emergencyCase.getCreatedAt()));
            ps.setString(9, DateTimeUtil.formatDateTime(emergencyCase.getUpdatedAt()));
            return JdbcSupport.executeInsert(ps);
        }
    }

    public void updateDisplayCode(long id, String displayCode) throws SQLException {
        String sql = "UPDATE emergency_case SET display_code = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setString(1, displayCode);
            ps.setString(2, DateTimeUtil.formatDateTime(DateTimeUtil.now()));
            ps.setLong(3, id);
            ps.executeUpdate();
        }
    }

    public void updateStatus(long id, CaseStatus status) throws SQLException {
        String sql = "UPDATE emergency_case SET status = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, DateTimeUtil.formatDateTime(DateTimeUtil.now()));
            ps.setLong(3, id);
            ps.executeUpdate();
        }
    }

    public void updateChiefComplaint(long id, String chiefComplaint) throws SQLException {
        String sql = "UPDATE emergency_case SET chief_complaint = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            JdbcSupport.setNullableString(ps, 1, chiefComplaint);
            ps.setString(2, DateTimeUtil.formatDateTime(DateTimeUtil.now()));
            ps.setLong(3, id);
            ps.executeUpdate();
        }
    }

    public Optional<EmergencyCase> findLongestWaitingOpen() throws SQLException {
        String sql = """
                SELECT id, display_code, patient_id, chief_complaint, arrival_time, status, case_type,
                       created_by, created_at, updated_at
                FROM emergency_case
                WHERE status NOT IN ('DISCHARGED', 'CANCELLED')
                ORDER BY arrival_time ASC
                LIMIT 1
                """;
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public long countByStatus(CaseStatus status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM emergency_case WHERE status = ?";
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    /**
     * Cases arriving between inclusive datetime bounds, optionally filtered by status.
     * Joins patient for display fields and left-joins triage priority.
     */
    public java.util.List<com.Clinivora.model.CaseReportRow> findReportRowsBetween(
            String startDateTime,
            String endDateTime,
            CaseStatus statusOrNull) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT c.id, c.display_code, c.status, c.arrival_time, c.chief_complaint,
                       p.display_code AS patient_code,
                       (p.first_name || ' ' || p.last_name) AS patient_name,
                       t.priority
                FROM emergency_case c
                JOIN patient p ON p.id = c.patient_id
                LEFT JOIN triage_assessment t ON t.case_id = c.id
                WHERE c.arrival_time >= ? AND c.arrival_time <= ?
                """);
        if (statusOrNull != null) {
            sql.append(" AND c.status = ?");
        }
        sql.append(" ORDER BY c.arrival_time");

        java.util.List<com.Clinivora.model.CaseReportRow> rows = new ArrayList<>();
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql.toString())) {
            ps.setString(1, startDateTime);
            ps.setString(2, endDateTime);
            if (statusOrNull != null) {
                ps.setString(3, statusOrNull.name());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    com.Clinivora.model.CaseReportRow row = new com.Clinivora.model.CaseReportRow();
                    row.setCaseId(rs.getLong("id"));
                    row.setCaseCode(rs.getString("display_code"));
                    row.setStatus(CaseStatus.fromString(rs.getString("status")));
                    row.setArrivalTime(DateTimeUtil.parseDateTime(rs.getString("arrival_time")));
                    row.setChiefComplaint(rs.getString("chief_complaint"));
                    row.setPatientCode(rs.getString("patient_code"));
                    row.setPatientName(rs.getString("patient_name"));
                    String priority = rs.getString("priority");
                    if (priority != null && !priority.isBlank()) {
                        row.setPriority(com.Clinivora.model.enums.TriagePriority.fromString(priority));
                    }
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    private EmergencyCase map(ResultSet rs) throws SQLException {
        EmergencyCase emergencyCase = new EmergencyCase();
        emergencyCase.setId(rs.getLong("id"));
        emergencyCase.setDisplayCode(rs.getString("display_code"));
        emergencyCase.setPatientId(rs.getLong("patient_id"));
        emergencyCase.setChiefComplaint(rs.getString("chief_complaint"));
        emergencyCase.setArrivalTime(DateTimeUtil.parseDateTime(rs.getString("arrival_time")));
        emergencyCase.setStatus(CaseStatus.fromString(rs.getString("status")));
        emergencyCase.setCaseType(CaseType.fromString(rs.getString("case_type")));
        long createdBy = rs.getLong("created_by");
        emergencyCase.setCreatedBy(rs.wasNull() ? null : createdBy);
        emergencyCase.setCreatedAt(DateTimeUtil.parseDateTime(rs.getString("created_at")));
        emergencyCase.setUpdatedAt(DateTimeUtil.parseDateTime(rs.getString("updated_at")));
        return emergencyCase;
    }
}

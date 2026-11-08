package com.Clinivora.repository;

import com.Clinivora.model.TriageAssessment;
import com.Clinivora.model.enums.TriagePriority;
import com.Clinivora.util.DateTimeUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class TriageAssessmentRepository {

    public Optional<TriageAssessment> findByCaseId(long caseId) throws SQLException {
        String sql = """
                SELECT id, case_id, priority, vital_signs, notes, assessed_by, assessed_at
                FROM triage_assessment WHERE case_id = ?
                """;
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setLong(1, caseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        }
        return Optional.empty();
    }

    public long insert(TriageAssessment assessment) throws SQLException {
        String sql = """
                INSERT INTO triage_assessment(case_id, priority, vital_signs, notes, assessed_by, assessed_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql, JdbcSupport.RETURN_GENERATED)) {
            ps.setLong(1, assessment.getCaseId());
            ps.setString(2, assessment.getPriority().name());
            JdbcSupport.setNullableString(ps, 3, assessment.getVitalSigns());
            JdbcSupport.setNullableString(ps, 4, assessment.getNotes());
            JdbcSupport.setNullableLong(ps, 5, assessment.getAssessedBy());
            ps.setString(6, DateTimeUtil.formatDateTime(assessment.getAssessedAt()));
            return JdbcSupport.executeInsert(ps);
        }
    }

    public void update(TriageAssessment assessment) throws SQLException {
        String sql = """
                UPDATE triage_assessment
                SET priority = ?, vital_signs = ?, notes = ?, assessed_by = ?, assessed_at = ?
                WHERE case_id = ?
                """;
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setString(1, assessment.getPriority().name());
            JdbcSupport.setNullableString(ps, 2, assessment.getVitalSigns());
            JdbcSupport.setNullableString(ps, 3, assessment.getNotes());
            JdbcSupport.setNullableLong(ps, 4, assessment.getAssessedBy());
            ps.setString(5, DateTimeUtil.formatDateTime(assessment.getAssessedAt()));
            ps.setLong(6, assessment.getCaseId());
            ps.executeUpdate();
        }
    }

    private TriageAssessment map(ResultSet rs) throws SQLException {
        TriageAssessment assessment = new TriageAssessment();
        assessment.setId(rs.getLong("id"));
        assessment.setCaseId(rs.getLong("case_id"));
        assessment.setPriority(TriagePriority.fromString(rs.getString("priority")));
        assessment.setVitalSigns(rs.getString("vital_signs"));
        assessment.setNotes(rs.getString("notes"));
        long assessedBy = rs.getLong("assessed_by");
        assessment.setAssessedBy(rs.wasNull() ? null : assessedBy);
        assessment.setAssessedAt(DateTimeUtil.parseDateTime(rs.getString("assessed_at")));
        return assessment;
    }
}

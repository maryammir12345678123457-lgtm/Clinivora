package com.Clinivora.repository;

import com.Clinivora.model.PatientEvent;
import com.Clinivora.model.enums.EventType;
import com.Clinivora.util.DateTimeUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PatientEventRepository {

    public List<PatientEvent> findByPatientId(long patientId) throws SQLException {
        String sql = """
                SELECT id, patient_id, case_id, event_type, description, created_by, created_at
                FROM patient_event WHERE patient_id = ? ORDER BY created_at, id
                """;
        List<PatientEvent> events = new ArrayList<>();
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setLong(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(map(rs));
                }
            }
        }
        return events;
    }

    public List<PatientEvent> findByCaseId(long caseId) throws SQLException {
        String sql = """
                SELECT id, patient_id, case_id, event_type, description, created_by, created_at
                FROM patient_event WHERE case_id = ? ORDER BY created_at, id
                """;
        List<PatientEvent> events = new ArrayList<>();
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setLong(1, caseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(map(rs));
                }
            }
        }
        return events;
    }

    public long insert(PatientEvent event) throws SQLException {
        String sql = """
                INSERT INTO patient_event(patient_id, case_id, event_type, description, created_by, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql, JdbcSupport.RETURN_GENERATED)) {
            ps.setLong(1, event.getPatientId());
            JdbcSupport.setNullableLong(ps, 2, event.getCaseId());
            ps.setString(3, event.getEventType().name());
            JdbcSupport.setNullableString(ps, 4, event.getDescription());
            JdbcSupport.setNullableLong(ps, 5, event.getCreatedBy());
            ps.setString(6, DateTimeUtil.formatDateTime(event.getCreatedAt()));
            return JdbcSupport.executeInsert(ps);
        }
    }

    private PatientEvent map(ResultSet rs) throws SQLException {
        PatientEvent event = new PatientEvent();
        event.setId(rs.getLong("id"));
        event.setPatientId(rs.getLong("patient_id"));
        long caseId = rs.getLong("case_id");
        event.setCaseId(rs.wasNull() ? null : caseId);
        event.setEventType(EventType.fromString(rs.getString("event_type")));
        event.setDescription(rs.getString("description"));
        long createdBy = rs.getLong("created_by");
        event.setCreatedBy(rs.wasNull() ? null : createdBy);
        event.setCreatedAt(DateTimeUtil.parseDateTime(rs.getString("created_at")));
        return event;
    }
}

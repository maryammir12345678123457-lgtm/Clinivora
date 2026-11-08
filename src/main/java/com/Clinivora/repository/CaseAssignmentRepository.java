package com.Clinivora.repository;

import com.Clinivora.model.CaseAssignment;
import com.Clinivora.model.enums.AssignmentStatus;
import com.Clinivora.util.DateTimeUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CaseAssignmentRepository {

    public Optional<CaseAssignment> findActiveByCaseId(long caseId) throws SQLException {
        String sql = """
                SELECT id, case_id, doctor_id, assigned_at, released_at, status, assigned_by
                FROM case_assignment WHERE case_id = ? AND status = 'ACTIVE'
                ORDER BY assigned_at DESC LIMIT 1
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

    public List<CaseAssignment> findActiveByDoctorId(long doctorId) throws SQLException {
        String sql = """
                SELECT id, case_id, doctor_id, assigned_at, released_at, status, assigned_by
                FROM case_assignment WHERE doctor_id = ? AND status = 'ACTIVE'
                ORDER BY assigned_at
                """;
        List<CaseAssignment> assignments = new ArrayList<>();
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setLong(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    assignments.add(map(rs));
                }
            }
        }
        return assignments;
    }

    public long insert(CaseAssignment assignment) throws SQLException {
        String sql = """
                INSERT INTO case_assignment(case_id, doctor_id, assigned_at, released_at, status, assigned_by)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql, JdbcSupport.RETURN_GENERATED)) {
            ps.setLong(1, assignment.getCaseId());
            ps.setLong(2, assignment.getDoctorId());
            ps.setString(3, DateTimeUtil.formatDateTime(assignment.getAssignedAt()));
            JdbcSupport.setNullableString(ps, 4, DateTimeUtil.formatDateTime(assignment.getReleasedAt()));
            ps.setString(5, assignment.getStatus().name());
            JdbcSupport.setNullableLong(ps, 6, assignment.getAssignedBy());
            return JdbcSupport.executeInsert(ps);
        }
    }

    public void complete(long id) throws SQLException {
        String sql = """
                UPDATE case_assignment
                SET status = 'COMPLETED', released_at = ?
                WHERE id = ?
                """;
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setString(1, DateTimeUtil.formatDateTime(DateTimeUtil.now()));
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public void reassign(long id) throws SQLException {
        String sql = """
                UPDATE case_assignment
                SET status = 'REASSIGNED', released_at = ?
                WHERE id = ?
                """;
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setString(1, DateTimeUtil.formatDateTime(DateTimeUtil.now()));
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public java.util.Map<String, Long> countAssignmentsByDoctor(String startDateTime, String endDateTime)
            throws SQLException {
        String sql = """
                SELECT d.full_name AS doctor_name, COUNT(*) AS cnt
                FROM case_assignment a
                JOIN doctor d ON d.id = a.doctor_id
                WHERE a.assigned_at >= ? AND a.assigned_at <= ?
                GROUP BY d.id, d.full_name
                ORDER BY cnt DESC, d.full_name
                """;
        java.util.Map<String, Long> counts = new java.util.LinkedHashMap<>();
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setString(1, startDateTime);
            ps.setString(2, endDateTime);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    counts.put(rs.getString("doctor_name"), rs.getLong("cnt"));
                }
            }
        }
        return counts;
    }

    private CaseAssignment map(ResultSet rs) throws SQLException {
        CaseAssignment assignment = new CaseAssignment();
        assignment.setId(rs.getLong("id"));
        assignment.setCaseId(rs.getLong("case_id"));
        assignment.setDoctorId(rs.getLong("doctor_id"));
        assignment.setAssignedAt(DateTimeUtil.parseDateTime(rs.getString("assigned_at")));
        assignment.setReleasedAt(DateTimeUtil.parseDateTime(rs.getString("released_at")));
        assignment.setStatus(AssignmentStatus.fromString(rs.getString("status")));
        long assignedBy = rs.getLong("assigned_by");
        assignment.setAssignedBy(rs.wasNull() ? null : assignedBy);
        return assignment;
    }
}

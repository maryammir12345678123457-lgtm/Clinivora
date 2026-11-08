package com.Clinivora.repository;

import com.Clinivora.model.ServiceTicket;
import com.Clinivora.model.enums.TicketStatus;
import com.Clinivora.util.DateTimeUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServiceTicketRepository {

    public Optional<ServiceTicket> findById(long id) throws SQLException {
        String sql = """
                SELECT id, display_code, patient_id, case_id, queue_number, status, created_at, called_at, completed_at
                FROM service_ticket WHERE id = ?
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

    public List<ServiceTicket> findByStatus(TicketStatus status) throws SQLException {
        String sql = """
                SELECT id, display_code, patient_id, case_id, queue_number, status, created_at, called_at, completed_at
                FROM service_ticket WHERE status = ? ORDER BY queue_number, created_at
                """;
        List<ServiceTicket> tickets = new ArrayList<>();
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tickets.add(map(rs));
                }
            }
        }
        return tickets;
    }

    public int nextQueueNumber() throws SQLException {
        String sql = "SELECT COALESCE(MAX(queue_number), 0) + 1 FROM service_ticket";
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public long insert(ServiceTicket ticket) throws SQLException {
        String sql = """
                INSERT INTO service_ticket(display_code, patient_id, case_id, queue_number, status,
                                           created_at, called_at, completed_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql, JdbcSupport.RETURN_GENERATED)) {
            ps.setString(1, ticket.getDisplayCode());
            ps.setLong(2, ticket.getPatientId());
            JdbcSupport.setNullableLong(ps, 3, ticket.getCaseId());
            JdbcSupport.setNullableInt(ps, 4, ticket.getQueueNumber());
            ps.setString(5, ticket.getStatus().name());
            ps.setString(6, DateTimeUtil.formatDateTime(ticket.getCreatedAt()));
            JdbcSupport.setNullableString(ps, 7, DateTimeUtil.formatDateTime(ticket.getCalledAt()));
            JdbcSupport.setNullableString(ps, 8, DateTimeUtil.formatDateTime(ticket.getCompletedAt()));
            return JdbcSupport.executeInsert(ps);
        }
    }

    public void updateDisplayCode(long id, String displayCode) throws SQLException {
        String sql = "UPDATE service_ticket SET display_code = ? WHERE id = ?";
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setString(1, displayCode);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public void updateStatus(long id, TicketStatus status) throws SQLException {
        String sql = """
                UPDATE service_ticket
                SET status = ?,
                    called_at = CASE WHEN ? = 'CALLED' THEN ? ELSE called_at END,
                    completed_at = CASE WHEN ? IN ('DONE', 'CANCELLED') THEN ? ELSE completed_at END
                WHERE id = ?
                """;
        String now = DateTimeUtil.formatDateTime(DateTimeUtil.now());
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, status.name());
            ps.setString(3, now);
            ps.setString(4, status.name());
            ps.setString(5, now);
            ps.setLong(6, id);
            ps.executeUpdate();
        }
    }

    public long countByStatus(TicketStatus status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM service_ticket WHERE status = ?";
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private ServiceTicket map(ResultSet rs) throws SQLException {
        ServiceTicket ticket = new ServiceTicket();
        ticket.setId(rs.getLong("id"));
        ticket.setDisplayCode(rs.getString("display_code"));
        ticket.setPatientId(rs.getLong("patient_id"));
        long caseId = rs.getLong("case_id");
        ticket.setCaseId(rs.wasNull() ? null : caseId);
        int queueNumber = rs.getInt("queue_number");
        ticket.setQueueNumber(rs.wasNull() ? null : queueNumber);
        ticket.setStatus(TicketStatus.fromString(rs.getString("status")));
        ticket.setCreatedAt(DateTimeUtil.parseDateTime(rs.getString("created_at")));
        ticket.setCalledAt(DateTimeUtil.parseDateTime(rs.getString("called_at")));
        ticket.setCompletedAt(DateTimeUtil.parseDateTime(rs.getString("completed_at")));
        return ticket;
    }
}

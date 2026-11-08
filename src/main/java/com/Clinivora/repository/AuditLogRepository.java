package com.Clinivora.repository;

import com.Clinivora.model.AuditLog;
import com.Clinivora.util.DateTimeUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuditLogRepository {

    public long insert(AuditLog log) throws SQLException {
        String sql = """
                INSERT INTO audit_log(user_id, action, entity_type, entity_id, details, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql, JdbcSupport.RETURN_GENERATED)) {
            JdbcSupport.setNullableLong(ps, 1, log.getUserId());
            ps.setString(2, log.getAction());
            JdbcSupport.setNullableString(ps, 3, log.getEntityType());
            JdbcSupport.setNullableLong(ps, 4, log.getEntityId());
            JdbcSupport.setNullableString(ps, 5, log.getDetails());
            ps.setString(6, DateTimeUtil.formatDateTime(log.getCreatedAt()));
            return JdbcSupport.executeInsert(ps);
        }
    }

    public List<AuditLog> findRecent(int limit) throws SQLException {
        return findFiltered(null, null, null, limit);
    }

    public List<AuditLog> findFiltered(String actionContains,
                                       String fromDateTime,
                                       String toDateTime,
                                       int limit) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT id, user_id, action, entity_type, entity_id, details, created_at
                FROM audit_log WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();
        if (actionContains != null && !actionContains.isBlank()) {
            sql.append(" AND lower(action) LIKE lower(?)");
            params.add("%" + actionContains.trim() + "%");
        }
        if (fromDateTime != null && !fromDateTime.isBlank()) {
            sql.append(" AND created_at >= ?");
            params.add(fromDateTime.trim());
        }
        if (toDateTime != null && !toDateTime.isBlank()) {
            sql.append(" AND created_at <= ?");
            params.add(toDateTime.trim());
        }
        sql.append(" ORDER BY created_at DESC, id DESC LIMIT ?");
        params.add(limit);

        List<AuditLog> logs = new ArrayList<>();
        try (PreparedStatement ps = JdbcSupport.connection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object value = params.get(i);
                if (value instanceof Integer integer) {
                    ps.setInt(i + 1, integer);
                } else {
                    ps.setString(i + 1, String.valueOf(value));
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(map(rs));
                }
            }
        }
        return logs;
    }

    private AuditLog map(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setId(rs.getLong("id"));
        long userId = rs.getLong("user_id");
        log.setUserId(rs.wasNull() ? null : userId);
        log.setAction(rs.getString("action"));
        log.setEntityType(rs.getString("entity_type"));
        long entityId = rs.getLong("entity_id");
        log.setEntityId(rs.wasNull() ? null : entityId);
        log.setDetails(rs.getString("details"));
        log.setCreatedAt(DateTimeUtil.parseDateTime(rs.getString("created_at")));
        return log;
    }
}

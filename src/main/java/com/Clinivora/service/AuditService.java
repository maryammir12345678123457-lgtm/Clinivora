package com.Clinivora.service;

import com.Clinivora.model.AuditLog;
import com.Clinivora.repository.AuditLogRepository;
import com.Clinivora.util.DateTimeUtil;

import java.sql.SQLException;
import java.util.List;

public class AuditService {
    private final AuditLogRepository auditLogRepository;
    private final UserSession session;

    public AuditService(AuditLogRepository auditLogRepository, UserSession session) {
        this.auditLogRepository = auditLogRepository;
        this.session = session;
    }

    public void record(String action, String entityType, Long entityId, String details) {
        try {
            AuditLog log = new AuditLog();
            log.setUserId(session.getUserId());
            log.setAction(action);
            log.setEntityType(entityType);
            log.setEntityId(entityId);
            log.setDetails(details);
            log.setCreatedAt(DateTimeUtil.now());
            auditLogRepository.insert(log);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to write audit log", e);
        }
    }

    public List<AuditLog> recent(int limit) {
        try {
            return auditLogRepository.findRecent(limit);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load audit logs", e);
        }
    }

    public List<AuditLog> search(String actionContains, String fromDateTime, String toDateTime, int limit) {
        try {
            return auditLogRepository.findFiltered(actionContains, fromDateTime, toDateTime, limit);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to search audit logs", e);
        }
    }
}

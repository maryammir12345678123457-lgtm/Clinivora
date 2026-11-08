package com.Clinivora.service;

import com.Clinivora.model.EmergencyCase;
import com.Clinivora.model.PatientEvent;
import com.Clinivora.model.enums.CaseStatus;
import com.Clinivora.model.enums.CaseType;
import com.Clinivora.model.enums.EventType;
import com.Clinivora.model.enums.UserRole;
import com.Clinivora.repository.EmergencyCaseRepository;
import com.Clinivora.repository.PatientEventRepository;
import com.Clinivora.repository.PatientRepository;
import com.Clinivora.util.DateTimeUtil;
import com.Clinivora.util.DisplayCodeFormatter;
import com.Clinivora.util.InputValidators;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EmergencyCaseService {
    private final EmergencyCaseRepository caseRepository;
    private final PatientRepository patientRepository;
    private final PatientEventRepository patientEventRepository;
    private final AuthorizationService authorizationService;
    private final AuditService auditService;
    private final UserSession session;

    public EmergencyCaseService(EmergencyCaseRepository caseRepository,
                                PatientRepository patientRepository,
                                PatientEventRepository patientEventRepository,
                                AuthorizationService authorizationService,
                                AuditService auditService,
                                UserSession session) {
        this.caseRepository = caseRepository;
        this.patientRepository = patientRepository;
        this.patientEventRepository = patientEventRepository;
        this.authorizationService = authorizationService;
        this.auditService = auditService;
        this.session = session;
    }

    public EmergencyCase openCase(long patientId, String chiefComplaint, CaseType caseType) {
        authorizationService.requireRole(UserRole.ADMIN, UserRole.RECEPTION, UserRole.TRIAGE);
        try {
            if (patientRepository.findById(patientId).isEmpty()) {
                throw new IllegalArgumentException("Patient not found: " + patientId);
            }
            EmergencyCase emergencyCase = new EmergencyCase();
            emergencyCase.setDisplayCode("TEMP");
            emergencyCase.setPatientId(patientId);
            emergencyCase.setChiefComplaint(
                    InputValidators.isBlank(chiefComplaint) ? null : chiefComplaint.trim());
            emergencyCase.setArrivalTime(DateTimeUtil.now());
            emergencyCase.setStatus(CaseStatus.REGISTERED);
            emergencyCase.setCaseType(caseType == null ? CaseType.EMERGENCY : caseType);
            emergencyCase.setCreatedBy(session.getUserId());
            emergencyCase.setCreatedAt(DateTimeUtil.now());
            emergencyCase.setUpdatedAt(emergencyCase.getCreatedAt());

            long id = caseRepository.insert(emergencyCase);
            String code = DisplayCodeFormatter.emergencyCase(id);
            caseRepository.updateDisplayCode(id, code);
            emergencyCase.setId(id);
            emergencyCase.setDisplayCode(code);

            PatientEvent event = new PatientEvent();
            event.setPatientId(patientId);
            event.setCaseId(id);
            event.setEventType(EventType.REGISTERED);
            event.setDescription("Case opened: " + code);
            event.setCreatedBy(session.getUserId());
            event.setCreatedAt(DateTimeUtil.now());
            patientEventRepository.insert(event);

            auditService.record("CASE_OPEN", "emergency_case", id, code);
            return emergencyCase;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to open case", e);
        }
    }

    public Optional<EmergencyCase> findById(long id) {
        try {
            return caseRepository.findById(id);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load case", e);
        }
    }

    public List<EmergencyCase> findOpen() {
        try {
            return caseRepository.findAllOpen();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list open cases", e);
        }
    }

    public List<EmergencyCase> findAwaitingTriage() {
        try {
            return caseRepository.findByStatus(CaseStatus.REGISTERED);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list triage queue", e);
        }
    }

    /**
     * Cases already assigned or in treatment — visible on the ER "Active floor" board.
     * These are open for the dashboard KPI but never appear in Needs Triage / Priority Queue.
     */
    public List<EmergencyCase> findActiveFloor() {
        try {
            return caseRepository.findByTypeAndStatuses(
                    CaseType.EMERGENCY, CaseStatus.ASSIGNED, CaseStatus.IN_TREATMENT);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list active floor cases", e);
        }
    }

    public void updateStatus(long caseId, CaseStatus status) {
        try {
            caseRepository.updateStatus(caseId, status);
            auditService.record("CASE_STATUS", "emergency_case", caseId, status.name());
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update case status", e);
        }
    }

    public void updateComplaint(long caseId, String chiefComplaint) {
        authorizationService.requireRole(UserRole.ADMIN, UserRole.TRIAGE);
        try {
            EmergencyCase emergencyCase = caseRepository.findById(caseId)
                    .orElseThrow(() -> new IllegalArgumentException("Case not found: " + caseId));
            if (emergencyCase.getStatus() == CaseStatus.DISCHARGED
                    || emergencyCase.getStatus() == CaseStatus.CANCELLED) {
                throw new IllegalStateException("Cannot edit a closed case");
            }
            String complaint = InputValidators.isBlank(chiefComplaint) ? null : chiefComplaint.trim();
            caseRepository.updateChiefComplaint(caseId, complaint);

            PatientEvent event = new PatientEvent();
            event.setPatientId(emergencyCase.getPatientId());
            event.setCaseId(caseId);
            event.setEventType(EventType.UPDATED);
            event.setDescription("Chief complaint updated");
            event.setCreatedBy(session.getUserId());
            event.setCreatedAt(DateTimeUtil.now());
            patientEventRepository.insert(event);

            auditService.record("CASE_COMPLAINT_UPDATE", "emergency_case", caseId, complaint);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update chief complaint", e);
        }
    }

    public void cancelCase(long caseId) {
        authorizationService.requireRole(UserRole.ADMIN, UserRole.RECEPTION, UserRole.TRIAGE);
        try {
            EmergencyCase emergencyCase = caseRepository.findById(caseId)
                    .orElseThrow(() -> new IllegalArgumentException("Case not found: " + caseId));
            if (emergencyCase.getStatus() == CaseStatus.DISCHARGED
                    || emergencyCase.getStatus() == CaseStatus.CANCELLED) {
                throw new IllegalStateException("Case is already closed");
            }
            caseRepository.updateStatus(caseId, CaseStatus.CANCELLED);

            PatientEvent event = new PatientEvent();
            event.setPatientId(emergencyCase.getPatientId());
            event.setCaseId(caseId);
            event.setEventType(EventType.OTHER);
            event.setDescription("Case cancelled");
            event.setCreatedBy(session.getUserId());
            event.setCreatedAt(DateTimeUtil.now());
            patientEventRepository.insert(event);

            auditService.record("CASE_CANCEL", "emergency_case", caseId, null);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to cancel case", e);
        }
    }
}

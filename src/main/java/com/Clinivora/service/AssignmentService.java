package com.Clinivora.service;

import com.Clinivora.model.CaseAssignment;
import com.Clinivora.model.EmergencyCase;
import com.Clinivora.model.PatientEvent;
import com.Clinivora.model.enums.AssignmentStatus;
import com.Clinivora.model.enums.CaseStatus;
import com.Clinivora.model.enums.EventType;
import com.Clinivora.model.enums.UserRole;
import com.Clinivora.repository.CaseAssignmentRepository;
import com.Clinivora.repository.DoctorRepository;
import com.Clinivora.repository.EmergencyCaseRepository;
import com.Clinivora.repository.PatientEventRepository;
import com.Clinivora.util.DateTimeUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AssignmentService {
    private final CaseAssignmentRepository assignmentRepository;
    private final EmergencyCaseRepository caseRepository;
    private final DoctorRepository doctorRepository;
    private final PatientEventRepository patientEventRepository;
    private final AuthorizationService authorizationService;
    private final AuditService auditService;
    private final UserSession session;

    public AssignmentService(CaseAssignmentRepository assignmentRepository,
                             EmergencyCaseRepository caseRepository,
                             DoctorRepository doctorRepository,
                             PatientEventRepository patientEventRepository,
                             AuthorizationService authorizationService,
                             AuditService auditService,
                             UserSession session) {
        this.assignmentRepository = assignmentRepository;
        this.caseRepository = caseRepository;
        this.doctorRepository = doctorRepository;
        this.patientEventRepository = patientEventRepository;
        this.authorizationService = authorizationService;
        this.auditService = auditService;
        this.session = session;
    }

    public CaseAssignment assign(long caseId, long doctorId) {
        authorizationService.requireRole(UserRole.ADMIN, UserRole.TRIAGE, UserRole.RECEPTION);
        try {
            EmergencyCase emergencyCase = caseRepository.findById(caseId)
                    .orElseThrow(() -> new IllegalArgumentException("Case not found: " + caseId));
            if (doctorRepository.findById(doctorId).isEmpty()) {
                throw new IllegalArgumentException("Doctor not found: " + doctorId);
            }

            Optional<CaseAssignment> existing = assignmentRepository.findActiveByCaseId(caseId);
            if (existing.isPresent()) {
                assignmentRepository.reassign(existing.get().getId());
            }

            CaseAssignment assignment = new CaseAssignment();
            assignment.setCaseId(caseId);
            assignment.setDoctorId(doctorId);
            assignment.setAssignedAt(DateTimeUtil.now());
            assignment.setStatus(AssignmentStatus.ACTIVE);
            assignment.setAssignedBy(session.getUserId());
            long id = assignmentRepository.insert(assignment);
            assignment.setId(id);

            caseRepository.updateStatus(caseId, CaseStatus.ASSIGNED);
            doctorRepository.setAvailable(doctorId, false);

            PatientEvent event = new PatientEvent();
            event.setPatientId(emergencyCase.getPatientId());
            event.setCaseId(caseId);
            event.setEventType(EventType.ASSIGNED);
            event.setDescription("Assigned to doctor #" + doctorId);
            event.setCreatedBy(session.getUserId());
            event.setCreatedAt(DateTimeUtil.now());
            patientEventRepository.insert(event);

            auditService.record("ASSIGN", "case_assignment", id, "case=" + caseId + ",doctor=" + doctorId);
            return assignment;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to assign case", e);
        }
    }

    public void startTreatment(long caseId) {
        authorizationService.requireRole(UserRole.ADMIN, UserRole.DOCTOR);
        try {
            EmergencyCase emergencyCase = caseRepository.findById(caseId)
                    .orElseThrow(() -> new IllegalArgumentException("Case not found: " + caseId));
            caseRepository.updateStatus(caseId, CaseStatus.IN_TREATMENT);

            PatientEvent event = new PatientEvent();
            event.setPatientId(emergencyCase.getPatientId());
            event.setCaseId(caseId);
            event.setEventType(EventType.TREATMENT_STARTED);
            event.setDescription("Treatment started");
            event.setCreatedBy(session.getUserId());
            event.setCreatedAt(DateTimeUtil.now());
            patientEventRepository.insert(event);
            auditService.record("TREATMENT_START", "emergency_case", caseId, null);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to start treatment", e);
        }
    }

    public void complete(long caseId) {
        authorizationService.requireRole(UserRole.ADMIN, UserRole.DOCTOR);
        try {
            EmergencyCase emergencyCase = caseRepository.findById(caseId)
                    .orElseThrow(() -> new IllegalArgumentException("Case not found: " + caseId));
            Optional<CaseAssignment> active = assignmentRepository.findActiveByCaseId(caseId);
            if (active.isPresent()) {
                assignmentRepository.complete(active.get().getId());
                doctorRepository.setAvailable(active.get().getDoctorId(), true);
            }
            caseRepository.updateStatus(caseId, CaseStatus.DISCHARGED);

            PatientEvent event = new PatientEvent();
            event.setPatientId(emergencyCase.getPatientId());
            event.setCaseId(caseId);
            event.setEventType(EventType.DISCHARGED);
            event.setDescription("Case discharged");
            event.setCreatedBy(session.getUserId());
            event.setCreatedAt(DateTimeUtil.now());
            patientEventRepository.insert(event);
            auditService.record("DISCHARGE", "emergency_case", caseId, null);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to complete case", e);
        }
    }

    public List<CaseAssignment> activeForDoctor(long doctorId) {
        try {
            return assignmentRepository.findActiveByDoctorId(doctorId);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load assignments", e);
        }
    }
}

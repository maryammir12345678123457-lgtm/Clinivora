package com.Clinivora.service;

import com.Clinivora.dsa.IndexedMinHeap;
import com.Clinivora.model.EmergencyCase;
import com.Clinivora.model.PatientEvent;
import com.Clinivora.model.TriageAssessment;
import com.Clinivora.model.enums.CaseStatus;
import com.Clinivora.model.enums.CaseType;
import com.Clinivora.model.enums.EventType;
import com.Clinivora.model.enums.TriagePriority;
import com.Clinivora.model.enums.UserRole;
import com.Clinivora.repository.EmergencyCaseRepository;
import com.Clinivora.repository.PatientEventRepository;
import com.Clinivora.repository.TriageAssessmentRepository;
import com.Clinivora.util.DateTimeUtil;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TriageService {
    public record PriorityCase(long caseId,
                               TriagePriority priority,
                               LocalDateTime arrivalTime,
                               String displayCode)
            implements IndexedMinHeap.PrioritizedCase {
        @Override
        public int priorityRank() {
            return priority.getRank();
        }

        @Override
        public LocalDateTime arrivalTime() {
            return arrivalTime;
        }

        @Override
        public long caseId() {
            return caseId;
        }
    }

    private final TriageAssessmentRepository triageRepository;
    private final EmergencyCaseRepository caseRepository;
    private final PatientEventRepository patientEventRepository;
    private final AuthorizationService authorizationService;
    private final AuditService auditService;
    private final UserSession session;

    public TriageService(TriageAssessmentRepository triageRepository,
                         EmergencyCaseRepository caseRepository,
                         PatientEventRepository patientEventRepository,
                         AuthorizationService authorizationService,
                         AuditService auditService,
                         UserSession session) {
        this.triageRepository = triageRepository;
        this.caseRepository = caseRepository;
        this.patientEventRepository = patientEventRepository;
        this.authorizationService = authorizationService;
        this.auditService = auditService;
        this.session = session;
    }

    public TriageAssessment assess(long caseId, TriagePriority priority, String vitalSigns, String notes) {
        authorizationService.requireRole(UserRole.ADMIN, UserRole.TRIAGE);
        if (priority == null) {
            throw new IllegalArgumentException("Priority is required");
        }
        try {
            EmergencyCase emergencyCase = caseRepository.findById(caseId)
                    .orElseThrow(() -> new IllegalArgumentException("Case not found: " + caseId));
            if (emergencyCase.getCaseType() != CaseType.EMERGENCY
                    && emergencyCase.getStatus() == CaseStatus.CANCELLED) {
                throw new IllegalStateException("Cannot triage cancelled case");
            }

            TriageAssessment assessment = new TriageAssessment();
            assessment.setCaseId(caseId);
            assessment.setPriority(priority);
            assessment.setVitalSigns(vitalSigns);
            assessment.setNotes(notes);
            assessment.setAssessedBy(session.getUserId());
            assessment.setAssessedAt(DateTimeUtil.now());

            Optional<TriageAssessment> existing = triageRepository.findByCaseId(caseId);
            if (existing.isPresent()) {
                assessment.setId(existing.get().getId());
                triageRepository.update(assessment);
            } else {
                long id = triageRepository.insert(assessment);
                assessment.setId(id);
            }

            if (emergencyCase.getStatus() != CaseStatus.ASSIGNED
                    && emergencyCase.getStatus() != CaseStatus.IN_TREATMENT) {
                caseRepository.updateStatus(caseId, CaseStatus.TRIAGED);
            }

            PatientEvent event = new PatientEvent();
            event.setPatientId(emergencyCase.getPatientId());
            event.setCaseId(caseId);
            event.setEventType(EventType.TRIAGED);
            event.setDescription("Triaged as " + priority.name());
            event.setCreatedBy(session.getUserId());
            event.setCreatedAt(DateTimeUtil.now());
            patientEventRepository.insert(event);

            auditService.record("TRIAGE", "emergency_case", caseId, priority.name());
            return assessment;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to triage case", e);
        }
    }

    /**
     * Triaged emergency cases waiting for doctor assignment (IndexedMinHeap order).
     * Assigned / in-treatment cases belong on the Active floor board instead.
     */
    public List<PriorityCase> buildPriorityQueue() {
        try {
            List<EmergencyCase> cases = caseRepository.findByTypeAndStatuses(
                    CaseType.EMERGENCY, CaseStatus.TRIAGED);
            IndexedMinHeap<PriorityCase> heap = IndexedMinHeap.forCases();
            for (EmergencyCase emergencyCase : cases) {
                Optional<TriageAssessment> assessment = triageRepository.findByCaseId(emergencyCase.getId());
                if (assessment.isEmpty()) {
                    continue;
                }
                heap.push(new PriorityCase(
                        emergencyCase.getId(),
                        assessment.get().getPriority(),
                        emergencyCase.getArrivalTime(),
                        emergencyCase.getDisplayCode()));
            }
            List<PriorityCase> ordered = new ArrayList<>();
            while (!heap.isEmpty()) {
                ordered.add(heap.pop());
            }
            return ordered;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to build priority queue", e);
        }
    }

    public Optional<TriageAssessment> findByCaseId(long caseId) {
        try {
            return triageRepository.findByCaseId(caseId);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load triage assessment", e);
        }
    }
}

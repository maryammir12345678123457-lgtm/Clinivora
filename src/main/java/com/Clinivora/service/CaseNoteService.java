package com.Clinivora.service;

import com.Clinivora.model.CaseNote;
import com.Clinivora.model.EmergencyCase;
import com.Clinivora.model.PatientEvent;
import com.Clinivora.model.enums.EventType;
import com.Clinivora.model.enums.UserRole;
import com.Clinivora.repository.CaseNoteRepository;
import com.Clinivora.repository.EmergencyCaseRepository;
import com.Clinivora.repository.PatientEventRepository;
import com.Clinivora.util.DateTimeUtil;
import com.Clinivora.util.InputValidators;

import java.sql.SQLException;
import java.util.List;

public class CaseNoteService {
    private final CaseNoteRepository caseNoteRepository;
    private final EmergencyCaseRepository caseRepository;
    private final PatientEventRepository patientEventRepository;
    private final AuthorizationService authorizationService;
    private final AuditService auditService;
    private final UserSession session;

    public CaseNoteService(CaseNoteRepository caseNoteRepository,
                           EmergencyCaseRepository caseRepository,
                           PatientEventRepository patientEventRepository,
                           AuthorizationService authorizationService,
                           AuditService auditService,
                           UserSession session) {
        this.caseNoteRepository = caseNoteRepository;
        this.caseRepository = caseRepository;
        this.patientEventRepository = patientEventRepository;
        this.authorizationService = authorizationService;
        this.auditService = auditService;
        this.session = session;
    }

    public CaseNote addNote(long caseId, String body) {
        authorizationService.requireRole(UserRole.ADMIN, UserRole.TRIAGE, UserRole.DOCTOR);
        String text = InputValidators.requireNonBlank(body, "Note");
        try {
            EmergencyCase emergencyCase = caseRepository.findById(caseId)
                    .orElseThrow(() -> new IllegalArgumentException("Case not found: " + caseId));
            CaseNote note = new CaseNote();
            note.setCaseId(caseId);
            note.setBody(text);
            note.setCreatedBy(session.getUserId());
            note.setCreatedAt(DateTimeUtil.now());
            long id = caseNoteRepository.insert(note);
            note.setId(id);

            PatientEvent event = new PatientEvent();
            event.setPatientId(emergencyCase.getPatientId());
            event.setCaseId(caseId);
            event.setEventType(EventType.CLINICAL_NOTE);
            event.setDescription(text.length() > 120 ? text.substring(0, 117) + "..." : text);
            event.setCreatedBy(session.getUserId());
            event.setCreatedAt(DateTimeUtil.now());
            patientEventRepository.insert(event);

            auditService.record("CASE_NOTE_ADD", "case_note", id, "case=" + caseId);
            return note;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to add clinical note", e);
        }
    }

    public List<CaseNote> listNotes(long caseId) {
        try {
            return caseNoteRepository.findByCaseId(caseId);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list clinical notes", e);
        }
    }
}

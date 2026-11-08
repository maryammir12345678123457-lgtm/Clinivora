package com.Clinivora.service;

import com.Clinivora.model.Patient;
import com.Clinivora.model.PatientEvent;
import com.Clinivora.model.enums.EventType;
import com.Clinivora.model.enums.UserRole;
import com.Clinivora.repository.PatientEventRepository;
import com.Clinivora.repository.PatientRepository;
import com.Clinivora.util.DateTimeUtil;
import com.Clinivora.util.DisplayCodeFormatter;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class PatientService {
    private final PatientRepository patientRepository;
    private final PatientEventRepository patientEventRepository;
    private final AuthorizationService authorizationService;
    private final ValidationService validationService;
    private final AuditService auditService;
    private final UserSession session;

    public PatientService(PatientRepository patientRepository,
                          PatientEventRepository patientEventRepository,
                          AuthorizationService authorizationService,
                          ValidationService validationService,
                          AuditService auditService,
                          UserSession session) {
        this.patientRepository = patientRepository;
        this.patientEventRepository = patientEventRepository;
        this.authorizationService = authorizationService;
        this.validationService = validationService;
        this.auditService = auditService;
        this.session = session;
    }

    public Patient register(String firstName,
                            String lastName,
                            LocalDate dateOfBirth,
                            String phone,
                            String gender,
                            String nationalId,
                            String address,
                            String emergencyContact) {
        authorizationService.requireRole(UserRole.ADMIN, UserRole.RECEPTION);
        try {
            Patient patient = new Patient();
            patient.setDisplayCode("TEMP");
            patient.setFirstName(validationService.requireName(firstName, "First name"));
            patient.setLastName(validationService.requireName(lastName, "Last name"));
            patient.setDateOfBirth(dateOfBirth);
            patient.setPhone(validationService.optionalPhone(phone));
            patient.setGender(gender);
            patient.setNationalId(nationalId);
            patient.setAddress(address);
            patient.setEmergencyContact(emergencyContact);
            patient.setActive(true);
            patient.setCreatedAt(DateTimeUtil.now());
            patient.setUpdatedAt(patient.getCreatedAt());

            long id = patientRepository.insert(patient);
            String code = DisplayCodeFormatter.patient(id);
            patientRepository.updateDisplayCode(id, code);
            patient.setId(id);
            patient.setDisplayCode(code);

            recordEvent(id, null, EventType.REGISTERED, "Patient registered");
            auditService.record("PATIENT_REGISTER", "patient", id, code);
            return patient;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to register patient", e);
        }
    }

    public Patient update(long patientId,
                          String firstName,
                          String lastName,
                          LocalDate dateOfBirth,
                          String phone,
                          String gender,
                          String nationalId,
                          String address,
                          String emergencyContact) {
        authorizationService.requireRole(UserRole.ADMIN, UserRole.TRIAGE, UserRole.RECEPTION);
        try {
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));
            patient.setFirstName(validationService.requireName(firstName, "First name"));
            patient.setLastName(validationService.requireName(lastName, "Last name"));
            patient.setDateOfBirth(dateOfBirth);
            patient.setPhone(validationService.optionalPhone(phone));
            patient.setGender(gender);
            patient.setNationalId(nationalId);
            patient.setAddress(address);
            patient.setEmergencyContact(emergencyContact);
            patientRepository.update(patient);
            recordEvent(patientId, null, EventType.UPDATED, "Patient demographics updated");
            auditService.record("PATIENT_UPDATE", "patient", patientId, patient.getDisplayCode());
            return patientRepository.findById(patientId).orElse(patient);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update patient", e);
        }
    }

    public void softDelete(long patientId) {
        authorizationService.requireRole(UserRole.ADMIN, UserRole.TRIAGE);
        try {
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));
            if (!patient.isActive()) {
                throw new IllegalStateException("Patient is already inactive");
            }
            patientRepository.setActive(patientId, false);
            recordEvent(patientId, null, EventType.SOFT_DELETED, "Patient soft-deleted");
            auditService.record("PATIENT_SOFT_DELETE", "patient", patientId, patient.getDisplayCode());
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to soft-delete patient", e);
        }
    }

    public void restore(long patientId) {
        authorizationService.requireRole(UserRole.ADMIN);
        try {
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));
            if (patient.isActive()) {
                throw new IllegalStateException("Patient is already active");
            }
            patientRepository.setActive(patientId, true);
            recordEvent(patientId, null, EventType.RESTORED, "Patient restored");
            auditService.record("PATIENT_RESTORE", "patient", patientId, patient.getDisplayCode());
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to restore patient", e);
        }
    }

    public Optional<Patient> findById(long id) {
        try {
            return patientRepository.findById(id);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load patient", e);
        }
    }

    public List<Patient> search(String query, boolean includeInactive) {
        try {
            if (includeInactive) {
                authorizationService.requireRole(UserRole.ADMIN);
            }
            if (query == null || query.isBlank()) {
                return patientRepository.findAll(includeInactive);
            }
            return patientRepository.searchByName(query.trim(), includeInactive);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to search patients", e);
        }
    }

    public List<Patient> search(String query) {
        return search(query, false);
    }

    public List<Patient> findAll() {
        return search(null, false);
    }

    private void recordEvent(long patientId, Long caseId, EventType type, String description) throws SQLException {
        PatientEvent event = new PatientEvent();
        event.setPatientId(patientId);
        event.setCaseId(caseId);
        event.setEventType(type);
        event.setDescription(description);
        event.setCreatedBy(session.getUserId());
        event.setCreatedAt(DateTimeUtil.now());
        patientEventRepository.insert(event);
    }
}

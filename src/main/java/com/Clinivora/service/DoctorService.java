package com.Clinivora.service;

import com.Clinivora.model.Doctor;
import com.Clinivora.model.enums.UserRole;
import com.Clinivora.repository.DoctorRepository;
import com.Clinivora.util.DateTimeUtil;
import com.Clinivora.util.DisplayCodeFormatter;
import com.Clinivora.util.InputValidators;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final AuthorizationService authorizationService;
    private final AuditService auditService;

    public DoctorService(DoctorRepository doctorRepository,
                         AuthorizationService authorizationService,
                         AuditService auditService) {
        this.doctorRepository = doctorRepository;
        this.authorizationService = authorizationService;
        this.auditService = auditService;
    }

    public Doctor create(String fullName, String specialty, Long userId) {
        authorizationService.requireRole(UserRole.ADMIN);
        if (InputValidators.isBlank(fullName)) {
            throw new IllegalArgumentException("Doctor name is required");
        }
        try {
            Doctor doctor = new Doctor();
            doctor.setDisplayCode("TEMP");
            doctor.setFullName(fullName.trim());
            doctor.setSpecialty(specialty);
            doctor.setUserId(userId);
            doctor.setAvailable(true);
            doctor.setCreatedAt(DateTimeUtil.now());
            long id = doctorRepository.insert(doctor);
            String code = DisplayCodeFormatter.doctor(id);
            doctorRepository.updateDisplayCode(id, code);
            doctor.setId(id);
            doctor.setDisplayCode(code);
            auditService.record("DOCTOR_CREATE", "doctor", id, code);
            return doctor;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create doctor", e);
        }
    }

    public List<Doctor> findAll() {
        return findAll(false);
    }

    public List<Doctor> findAll(boolean includeInactive) {
        try {
            return doctorRepository.findAll(includeInactive);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list doctors", e);
        }
    }

    public List<Doctor> findAvailable() {
        try {
            return doctorRepository.findAvailable();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list available doctors", e);
        }
    }

    public Optional<Doctor> findById(long id) {
        try {
            return doctorRepository.findById(id);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load doctor", e);
        }
    }

    public void setAvailable(long doctorId, boolean available) {
        try {
            doctorRepository.setAvailable(doctorId, available);
            auditService.record("DOCTOR_AVAILABILITY", "doctor", doctorId, String.valueOf(available));
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update doctor availability", e);
        }
    }

    public void setActive(long doctorId, boolean active) {
        authorizationService.requireRole(UserRole.ADMIN);
        try {
            doctorRepository.setActive(doctorId, active);
            if (!active) {
                doctorRepository.setAvailable(doctorId, false);
            }
            auditService.record(active ? "DOCTOR_ACTIVATE" : "DOCTOR_DEACTIVATE",
                    "doctor", doctorId, String.valueOf(active));
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update doctor active status", e);
        }
    }
}

package com.Clinivora.service;

import com.Clinivora.model.AppUser;
import com.Clinivora.model.Doctor;
import com.Clinivora.model.enums.UserRole;
import com.Clinivora.repository.AppUserRepository;
import com.Clinivora.repository.DoctorRepository;
import com.Clinivora.util.DateTimeUtil;
import com.Clinivora.util.DisplayCodeFormatter;
import com.Clinivora.util.PasswordHasher;

import java.sql.SQLException;

public class DemoDataSeeder {
    private final AppUserRepository userRepository;
    private final DoctorRepository doctorRepository;

    public DemoDataSeeder(AppUserRepository userRepository, DoctorRepository doctorRepository) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
    }

    public void seedIfNeeded() {
        try {
            ensureUser("admin", "AdminDemo!2026", UserRole.ADMIN, "System Administrator");
            ensureUser("reception", "ReceptionDemo!2026", UserRole.RECEPTION, "Front Desk");
            ensureUser("triage", "TriageDemo!2026", UserRole.TRIAGE, "Triage Nurse");
            AppUser doctorUser = ensureUser("doctor", "DoctorDemo!2026", UserRole.DOCTOR, "Dr. Demo");
            ensureDoctorProfile(doctorUser);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to seed demo data", e);
        }
    }

    private AppUser ensureUser(String username, String password, UserRole role, String displayName)
            throws SQLException {
        var existing = userRepository.findByUsername(username);
        if (existing.isPresent()) {
            return existing.get();
        }
        String salt = PasswordHasher.generateSalt();
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setSalt(salt);
        user.setPasswordHash(PasswordHasher.hash(password, salt));
        user.setRole(role);
        user.setDisplayName(displayName);
        user.setActive(true);
        user.setCreatedAt(DateTimeUtil.now());
        user.setUpdatedAt(user.getCreatedAt());
        long id = userRepository.insert(user);
        user.setId(id);
        return user;
    }

    private void ensureDoctorProfile(AppUser doctorUser) throws SQLException {
        if (doctorRepository.findByUserId(doctorUser.getId()).isPresent()) {
            return;
        }
        Doctor doctor = new Doctor();
        doctor.setUserId(doctorUser.getId());
        doctor.setDisplayCode("TEMP");
        doctor.setFullName(doctorUser.getDisplayName());
        doctor.setSpecialty("General Medicine");
        doctor.setAvailable(true);
        doctor.setCreatedAt(DateTimeUtil.now());
        long id = doctorRepository.insert(doctor);
        doctorRepository.updateDisplayCode(id, DisplayCodeFormatter.doctor(id));
    }
}

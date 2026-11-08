package com.Clinivora.service;

import com.Clinivora.model.enums.UserRole;

public class AuthorizationService {
    private final UserSession session;

    public AuthorizationService(UserSession session) {
        this.session = session;
    }

    public boolean hasRole(UserRole... roles) {
        if (!session.isAuthenticated() || roles == null) {
            return false;
        }
        UserRole current = session.getRole();
        for (UserRole role : roles) {
            if (current == role) {
                return true;
            }
        }
        return false;
    }

    public void requireRole(UserRole... roles) {
        if (!hasRole(roles)) {
            throw new SecurityException("Access denied for role: " + session.getRole());
        }
    }

    public boolean isElevatedClinical() {
        return hasRole(UserRole.ADMIN, UserRole.TRIAGE);
    }

    public boolean canManageUsers() {
        return hasRole(UserRole.ADMIN);
    }

    public boolean canRegisterPatients() {
        return hasRole(UserRole.ADMIN, UserRole.RECEPTION);
    }

    public boolean canEditPatients() {
        return hasRole(UserRole.ADMIN, UserRole.TRIAGE, UserRole.RECEPTION);
    }

    public boolean canSoftDeletePatients() {
        return hasRole(UserRole.ADMIN, UserRole.TRIAGE);
    }

    public boolean canRestorePatients() {
        return hasRole(UserRole.ADMIN);
    }

    public boolean canTriage() {
        return hasRole(UserRole.ADMIN, UserRole.TRIAGE);
    }

    public boolean canAssign() {
        return hasRole(UserRole.ADMIN, UserRole.TRIAGE, UserRole.RECEPTION);
    }

    public boolean canTreat() {
        return hasRole(UserRole.ADMIN, UserRole.DOCTOR);
    }

    public boolean canAddClinicalNotes() {
        return hasRole(UserRole.ADMIN, UserRole.TRIAGE, UserRole.DOCTOR);
    }

    public boolean canEditCaseComplaint() {
        return hasRole(UserRole.ADMIN, UserRole.TRIAGE);
    }

    public boolean canDeactivateDoctors() {
        return hasRole(UserRole.ADMIN);
    }
}

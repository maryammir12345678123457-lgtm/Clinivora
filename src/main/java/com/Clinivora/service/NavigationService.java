package com.Clinivora.service;

import com.Clinivora.dsa.ArrayStack;
import com.Clinivora.model.enums.UserRole;

public class NavigationService {
    public enum View {
        LOGIN("/com/Clinivora/view/LoginView.fxml", null),
        DASHBOARD("/com/Clinivora/view/DashboardView.fxml", "page.dashboard"),
        PATIENTS("/com/Clinivora/view/PatientsView.fxml", "page.patients"),
        PATIENT_PROFILE("/com/Clinivora/view/PatientProfileView.fxml", "page.patientProfile"),
        EMERGENCY_ROOM("/com/Clinivora/view/EmergencyRoomView.fxml", "page.emergency"),
        CASE_DETAIL("/com/Clinivora/view/CaseDetailView.fxml", "page.caseDetail"),
        ROUTINE_QUEUE("/com/Clinivora/view/RoutineQueueView.fxml", "page.queue"),
        DOCTORS("/com/Clinivora/view/DoctorsView.fxml", "page.doctors"),
        REPORTS("/com/Clinivora/view/ReportsView.fxml", "page.reports"),
        ACCOUNTS("/com/Clinivora/view/AccountsView.fxml", "page.accounts"),
        AUDIT_LOG("/com/Clinivora/view/AuditLogView.fxml", "page.audit"),
        DSA_LAB("/com/Clinivora/view/DsaLabView.fxml", "page.dsa"),
        SETTINGS("/com/Clinivora/view/SettingsView.fxml", "page.settings");

        private final String fxmlPath;
        private final String titleKey;

        View(String fxmlPath, String titleKey) {
            this.fxmlPath = fxmlPath;
            this.titleKey = titleKey;
        }

        public String getFxmlPath() {
            return fxmlPath;
        }

        /** @deprecated use {@link #getTitleKey()} with I18n */
        public String getTitle() {
            return titleKey;
        }

        public String getTitleKey() {
            return titleKey;
        }
    }

    private final ArrayStack<View> history = new ArrayStack<>();
    private final AuthorizationService authorizationService;
    private View current = View.LOGIN;
    private Long selectedPatientId;
    private Long selectedCaseId;
    private Runnable onNavigate;

    public NavigationService(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    public void setOnNavigate(Runnable onNavigate) {
        this.onNavigate = onNavigate;
    }

    public View getCurrent() {
        return current;
    }

    public Long getSelectedPatientId() {
        return selectedPatientId;
    }

    public void setSelectedPatientId(Long selectedPatientId) {
        this.selectedPatientId = selectedPatientId;
    }

    public Long getSelectedCaseId() {
        return selectedCaseId;
    }

    public void setSelectedCaseId(Long selectedCaseId) {
        this.selectedCaseId = selectedCaseId;
    }

    public void goTo(View view) {
        if (view == null) {
            throw new IllegalArgumentException("View is required");
        }
        ensureAllowed(view);
        if (current != view) {
            history.push(current);
        }
        current = view;
        notifyNavigate();
    }

    public View back() {
        if (history.isEmpty()) {
            return current;
        }
        current = history.pop();
        notifyNavigate();
        return current;
    }

    public void resetTo(View view) {
        history.clear();
        current = view;
        notifyNavigate();
    }

    public boolean isVisibleForCurrentRole(View view) {
        UserRole role = authorizationService.hasRole(UserRole.ADMIN) ? UserRole.ADMIN
                : authorizationService.hasRole(UserRole.RECEPTION) ? UserRole.RECEPTION
                : authorizationService.hasRole(UserRole.TRIAGE) ? UserRole.TRIAGE
                : authorizationService.hasRole(UserRole.DOCTOR) ? UserRole.DOCTOR
                : null;
        if (role == null || view == View.LOGIN || view == View.PATIENT_PROFILE || view == View.CASE_DETAIL) {
            return false;
        }
        return switch (view) {
            case DASHBOARD, DSA_LAB, SETTINGS -> true;
            case PATIENTS -> role == UserRole.ADMIN || role == UserRole.RECEPTION || role == UserRole.TRIAGE;
            case ROUTINE_QUEUE -> role == UserRole.ADMIN || role == UserRole.RECEPTION;
            case EMERGENCY_ROOM -> true;
            case DOCTORS -> role == UserRole.ADMIN || role == UserRole.DOCTOR;
            case REPORTS, ACCOUNTS, AUDIT_LOG -> role == UserRole.ADMIN;
            default -> false;
        };
    }

    private void notifyNavigate() {
        if (onNavigate != null) {
            onNavigate.run();
        }
    }

    private void ensureAllowed(View view) {
        if (view == View.LOGIN) {
            return;
        }
        if (!authorizationService.hasRole(
                UserRole.ADMIN, UserRole.RECEPTION, UserRole.TRIAGE, UserRole.DOCTOR)) {
            throw new SecurityException("Authentication required");
        }
        switch (view) {
            case PATIENTS ->
                    authorizationService.requireRole(UserRole.ADMIN, UserRole.RECEPTION, UserRole.TRIAGE);
            case ROUTINE_QUEUE ->
                    authorizationService.requireRole(UserRole.ADMIN, UserRole.RECEPTION);
            case EMERGENCY_ROOM, CASE_DETAIL ->
                    authorizationService.requireRole(
                            UserRole.ADMIN, UserRole.TRIAGE, UserRole.RECEPTION, UserRole.DOCTOR);
            case PATIENT_PROFILE ->
                    authorizationService.requireRole(
                            UserRole.ADMIN, UserRole.RECEPTION, UserRole.TRIAGE, UserRole.DOCTOR);
            case DOCTORS ->
                    authorizationService.requireRole(UserRole.ADMIN, UserRole.DOCTOR);
            case REPORTS, ACCOUNTS, AUDIT_LOG ->
                    authorizationService.requireRole(UserRole.ADMIN);
            case DASHBOARD, DSA_LAB, SETTINGS, LOGIN -> {
                // all authenticated roles
            }
        }
    }
}

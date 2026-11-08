package com.Clinivora.service;

import com.Clinivora.config.AppConfig;
import com.Clinivora.config.AppContext;
import com.Clinivora.config.DatabaseManager;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

abstract class ServiceTestSupport {
    @TempDir
    Path tempDir;

    protected AppContext context;

    protected void boot() {
        AppConfig.setDataDirectory(tempDir);
        DatabaseManager.resetForTests();
        AppContext.resetForTests();
        context = AppContext.getInstance();
        context.bootstrap();
    }

    protected void shutdown() {
        DatabaseManager.resetForTests();
        AppContext.resetForTests();
    }

    protected void loginAdmin() {
        context.getAuthenticationService().login("admin", "AdminDemo!2026");
    }

    protected void loginReception() {
        context.getAuthenticationService().login("reception", "ReceptionDemo!2026");
    }

    protected void loginTriage() {
        context.getAuthenticationService().login("triage", "TriageDemo!2026");
    }

    protected void loginDoctor() {
        context.getAuthenticationService().login("doctor", "DoctorDemo!2026");
    }

    protected void logout() {
        context.getAuthenticationService().logout();
    }
}

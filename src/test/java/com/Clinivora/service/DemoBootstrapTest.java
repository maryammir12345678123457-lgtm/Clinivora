package com.Clinivora.service;

import com.Clinivora.config.AppConfig;
import com.Clinivora.config.AppContext;
import com.Clinivora.config.DatabaseManager;
import com.Clinivora.model.AppUser;
import com.Clinivora.model.enums.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DemoBootstrapTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        AppConfig.setDataDirectory(tempDir);
        DatabaseManager.resetForTests();
        AppContext.resetForTests();
    }

    @AfterEach
    void tearDown() {
        DatabaseManager.resetForTests();
        AppContext.resetForTests();
    }

    @Test
    void seedsDemoAccountsIdempotentlyAndAllowsLogin() {
        AppContext context = AppContext.getInstance();
        context.bootstrap();
        context.bootstrap(); // idempotent

        AppUser admin = context.getAuthenticationService().login("admin", "AdminDemo!2026");
        assertEquals(UserRole.ADMIN, admin.getRole());
        context.getAuthenticationService().logout();

        assertEquals(UserRole.RECEPTION,
                context.getAuthenticationService().login("reception", "ReceptionDemo!2026").getRole());
        context.getAuthenticationService().logout();

        assertEquals(UserRole.TRIAGE,
                context.getAuthenticationService().login("triage", "TriageDemo!2026").getRole());
        context.getAuthenticationService().logout();

        assertEquals(UserRole.DOCTOR,
                context.getAuthenticationService().login("doctor", "DoctorDemo!2026").getRole());
    }
}

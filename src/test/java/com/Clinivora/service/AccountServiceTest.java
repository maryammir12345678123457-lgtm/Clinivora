package com.Clinivora.service;

import com.Clinivora.model.AppUser;
import com.Clinivora.model.enums.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountServiceTest extends ServiceTestSupport {

    @BeforeEach
    void setUp() {
        boot();
    }

    @AfterEach
    void tearDown() {
        shutdown();
    }

    @Test
    void adminCanCreateUserResetPasswordAndDeactivate() {
        loginAdmin();
        AppUser created = context.getAccountService().createUser(
                "nurse1", "Night Nurse", UserRole.TRIAGE, "NurseDemo!2026");
        assertEquals(UserRole.TRIAGE, created.getRole());
        assertTrue(created.isActive());

        context.getAccountService().resetPassword(created.getId(), "NurseReset!2026");
        logout();

        AppUser loggedIn = context.getAuthenticationService().login("nurse1", "NurseReset!2026");
        assertEquals(created.getId(), loggedIn.getId());
        logout();

        loginAdmin();
        context.getAccountService().setActive(created.getId(), false);
        logout();

        assertThrows(SecurityException.class,
                () -> context.getAuthenticationService().login("nurse1", "NurseReset!2026"));
    }

    @Test
    void createUserNormalizesUsernameAndAcceptsDemoStylePassword() {
        loginAdmin();
        AppUser created = context.getAccountService().createUser(
                "Nurse.Day", "Day Nurse", UserRole.RECEPTION, "NurseDemo!2026");
        assertEquals("nurse.day", created.getUsername());
        logout();

        AppUser loggedIn = context.getAuthenticationService().login("Nurse.Day", "NurseDemo!2026");
        assertEquals("nurse.day", loggedIn.getUsername());
    }

    @Test
    void createUserRequiresRole() {
        loginAdmin();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                context.getAccountService().createUser(
                        "norole", "No Role", null, "NurseDemo!2026"));
        assertEquals("Select a role", ex.getMessage());
    }

    @Test
    void requireUsernameGivesClearMessages() {
        ValidationService validation = context.getValidationService();
        assertEquals("Username is required",
                assertThrows(IllegalArgumentException.class, () -> validation.requireUsername("  ")).getMessage());
        assertEquals("Username must be at least 3 characters",
                assertThrows(IllegalArgumentException.class, () -> validation.requireUsername("ab")).getMessage());
        assertTrue(assertThrows(IllegalArgumentException.class, () -> validation.requireUsername("bad name"))
                .getMessage().contains("letters, numbers"));
        assertEquals("ok_user", validation.requireUsername("Ok_User"));
    }

    @Test
    void userCanChangeOwnPassword() {
        loginReception();
        context.getAccountService().changePassword(
                "ReceptionDemo!2026", "ReceptionNew!2026", "ReceptionNew!2026");
        logout();

        assertDoesNotThrow(() ->
                context.getAuthenticationService().login("reception", "ReceptionNew!2026"));
    }

    @Test
    void nonAdminCannotCreateUser() {
        loginDoctor();
        assertThrows(SecurityException.class, () ->
                context.getAccountService().createUser(
                        "xuser", "X", UserRole.RECEPTION, "XuserDemo!2026"));
    }
}

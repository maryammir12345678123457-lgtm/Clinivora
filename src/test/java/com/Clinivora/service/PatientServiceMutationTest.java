package com.Clinivora.service;

import com.Clinivora.model.Patient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PatientServiceMutationTest extends ServiceTestSupport {

    @BeforeEach
    void setUp() {
        boot();
    }

    @AfterEach
    void tearDown() {
        shutdown();
    }

    @Test
    void receptionCanRegisterAndEditButNotSoftDelete() {
        loginReception();
        Patient patient = context.getPatientService().register(
                "Ada", "Lovelace", LocalDate.of(1990, 1, 1), "+1234567890",
                "Female", null, "Addr", "Contact");
        assertTrue(patient.isActive());

        Patient updated = context.getPatientService().update(
                patient.getId(), "Ada", "Byron", LocalDate.of(1990, 1, 1),
                "+1234567890", "Female", null, "Addr2", "Contact");
        assertEquals("Byron", updated.getLastName());

        assertThrows(SecurityException.class,
                () -> context.getPatientService().softDelete(patient.getId()));
    }

    @Test
    void triageCanSoftDeleteAndAdminCanRestore() {
        loginReception();
        Patient patient = context.getPatientService().register(
                "Grace", "Hopper", null, null, null, null, null, null);
        logout();

        loginTriage();
        context.getPatientService().softDelete(patient.getId());
        assertFalse(context.getPatientService().findById(patient.getId()).orElseThrow().isActive());
        assertTrue(context.getPatientService().search(null, false).stream()
                .noneMatch(p -> p.getId() == patient.getId()));
        logout();

        loginAdmin();
        context.getPatientService().restore(patient.getId());
        assertTrue(context.getPatientService().findById(patient.getId()).orElseThrow().isActive());
    }

    @Test
    void doctorCannotSoftDeletePatient() {
        loginReception();
        Patient patient = context.getPatientService().register(
                "Alan", "Turing", null, null, null, null, null, null);
        logout();

        loginDoctor();
        assertThrows(SecurityException.class,
                () -> context.getPatientService().softDelete(patient.getId()));
    }
}

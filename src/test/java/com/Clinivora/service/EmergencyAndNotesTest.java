package com.Clinivora.service;

import com.Clinivora.model.CaseNote;
import com.Clinivora.model.EmergencyCase;
import com.Clinivora.model.Patient;
import com.Clinivora.model.enums.CaseType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmergencyAndNotesTest extends ServiceTestSupport {

    @BeforeEach
    void setUp() {
        boot();
    }

    @AfterEach
    void tearDown() {
        shutdown();
    }

    @Test
    void triageCanUpdateComplaintAndDoctorCanAddNotes() {
        loginReception();
        Patient patient = context.getPatientService().register(
                "Florence", "Nightingale", null, null, null, null, null, null);
        EmergencyCase emergencyCase = context.getEmergencyCaseService()
                .openCase(patient.getId(), "Chest pain", CaseType.EMERGENCY);
        logout();

        loginTriage();
        context.getEmergencyCaseService().updateComplaint(emergencyCase.getId(), "Severe chest pain");
        assertEquals("Severe chest pain",
                context.getEmergencyCaseService().findById(emergencyCase.getId()).orElseThrow()
                        .getChiefComplaint());
        logout();

        loginDoctor();
        CaseNote note = context.getCaseNoteService().addNote(emergencyCase.getId(), "ECG ordered");
        assertTrue(note.getId() > 0);
        List<CaseNote> notes = context.getCaseNoteService().listNotes(emergencyCase.getId());
        assertEquals(1, notes.size());
        assertEquals("ECG ordered", notes.get(0).getBody());
    }

    @Test
    void receptionCannotUpdateComplaintOrAddClinicalNotes() {
        loginReception();
        Patient patient = context.getPatientService().register(
                "Marie", "Curie", null, null, null, null, null, null);
        EmergencyCase emergencyCase = context.getEmergencyCaseService()
                .openCase(patient.getId(), "Fever", CaseType.EMERGENCY);

        assertThrows(SecurityException.class, () ->
                context.getEmergencyCaseService().updateComplaint(emergencyCase.getId(), "High fever"));
        assertThrows(SecurityException.class, () ->
                context.getCaseNoteService().addNote(emergencyCase.getId(), "Note"));
    }
}

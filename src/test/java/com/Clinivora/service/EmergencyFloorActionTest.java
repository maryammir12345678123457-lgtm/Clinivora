package com.Clinivora.service;

import com.Clinivora.model.EmergencyCase;
import com.Clinivora.model.Patient;
import com.Clinivora.model.enums.CaseStatus;
import com.Clinivora.model.enums.CaseType;
import com.Clinivora.model.enums.TriagePriority;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmergencyFloorActionTest extends ServiceTestSupport {

    @BeforeEach
    void setUp() {
        boot();
    }

    @AfterEach
    void tearDown() {
        shutdown();
    }

    @Test
    void setPriorityThenSendToFloorStartAndDischarge() {
        loginReception();
        Patient patient = context.getPatientService().register(
                "Maryam", "Naveed", null, null, null, null, null, null);
        EmergencyCase emergencyCase = context.getEmergencyCaseService()
                .openCase(patient.getId(), "Pain", CaseType.EMERGENCY);
        logout();

        loginTriage();
        context.getTriageService().assess(emergencyCase.getId(), TriagePriority.P4, null, null);
        assertEquals(1, context.getTriageService().buildPriorityQueue().size());

        context.getTriageService().assess(emergencyCase.getId(), TriagePriority.P1, null, "Upgraded");
        assertEquals(TriagePriority.P1, context.getTriageService().findByCaseId(emergencyCase.getId())
                .orElseThrow().getPriority());
        assertEquals(1, context.getTriageService().buildPriorityQueue().size());

        long doctorId = context.getDoctorService().findAvailable().get(0).getId();
        context.getAssignmentService().assign(emergencyCase.getId(), doctorId);
        assertTrue(context.getTriageService().buildPriorityQueue().isEmpty());
        assertEquals(CaseStatus.ASSIGNED, context.getEmergencyCaseService().findActiveFloor().get(0).getStatus());
        logout();

        loginAdmin();
        context.getTriageService().assess(emergencyCase.getId(), TriagePriority.P2, null, "Floor ESI");
        assertEquals(CaseStatus.ASSIGNED, context.getEmergencyCaseService().findById(emergencyCase.getId())
                .orElseThrow().getStatus());
        assertEquals(TriagePriority.P2, context.getTriageService().findByCaseId(emergencyCase.getId())
                .orElseThrow().getPriority());

        context.getAssignmentService().startTreatment(emergencyCase.getId());
        assertEquals(CaseStatus.IN_TREATMENT, context.getEmergencyCaseService().findActiveFloor().get(0).getStatus());

        context.getAssignmentService().complete(emergencyCase.getId());
        assertTrue(context.getEmergencyCaseService().findActiveFloor().isEmpty());
        assertEquals(0, context.getDashboardService().loadSnapshot().openErCases());
    }
}

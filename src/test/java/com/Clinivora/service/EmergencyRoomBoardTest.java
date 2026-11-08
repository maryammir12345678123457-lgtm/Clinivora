package com.Clinivora.service;

import com.Clinivora.model.Doctor;
import com.Clinivora.model.EmergencyCase;
import com.Clinivora.model.Patient;
import com.Clinivora.model.enums.CaseStatus;
import com.Clinivora.model.enums.CaseType;
import com.Clinivora.model.enums.TriagePriority;
import com.Clinivora.service.dashboard.DashboardSnapshot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Guards the Dashboard ↔ ER board gap: open ER KPI must be covered by
 * Needs Triage + Priority Queue + Active floor (no silent disappearances).
 */
class EmergencyRoomBoardTest extends ServiceTestSupport {

    @BeforeEach
    void setUp() {
        boot();
    }

    @AfterEach
    void tearDown() {
        shutdown();
    }

    @Test
    void inTreatmentCaseAppearsOnActiveFloorWhenDashboardShowsOpenEr() {
        loginReception();
        Patient patient = context.getPatientService().register(
                "Ghyias", "Mir", null, null, null, null, null, null);
        EmergencyCase emergencyCase = context.getEmergencyCaseService()
                .openCase(patient.getId(), "Chest pain", CaseType.EMERGENCY);
        logout();

        loginTriage();
        context.getTriageService().assess(emergencyCase.getId(), TriagePriority.P2, "BP high", "Urgent");
        Doctor doctor = context.getDoctorService().findAvailable().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Expected seeded available doctor"));
        context.getAssignmentService().assign(emergencyCase.getId(), doctor.getId());
        logout();

        loginDoctor();
        context.getAssignmentService().startTreatment(emergencyCase.getId());
        logout();

        loginAdmin();
        DashboardSnapshot snap = context.getDashboardService().loadSnapshot();
        assertEquals(1, snap.openErCases());
        assertEquals(1, snap.inTreatment());
        assertEquals(0, snap.awaitingTriage());

        List<EmergencyCase> awaiting = context.getEmergencyCaseService().findAwaitingTriage();
        List<TriageService.PriorityCase> priority = context.getTriageService().buildPriorityQueue();
        List<EmergencyCase> active = context.getEmergencyCaseService().findActiveFloor();

        assertTrue(awaiting.isEmpty(), "IN_TREATMENT must not sit in Needs Triage");
        assertTrue(priority.isEmpty(), "IN_TREATMENT must not sit in Priority Queue");
        assertEquals(1, active.size(), "IN_TREATMENT must appear on Active floor");
        assertEquals(emergencyCase.getId(), active.get(0).getId());
        assertEquals(CaseStatus.IN_TREATMENT, active.get(0).getStatus());

        assertEquals(snap.openErCases(), awaiting.size() + priority.size() + active.size(),
                "Dashboard open ER count must equal ER board coverage");
    }

    @Test
    void openCaseMovesAcrossErBoardsThroughLifecycle() {
        loginReception();
        Patient patient = context.getPatientService().register(
                "Florence", "Nightingale", null, null, null, null, null, null);
        EmergencyCase emergencyCase = context.getEmergencyCaseService()
                .openCase(patient.getId(), "Fever", CaseType.EMERGENCY);
        logout();

        loginAdmin();
        assertBoardCoverage(1);
        assertTrue(context.getEmergencyCaseService().findAwaitingTriage().stream()
                .anyMatch(c -> c.getId() == emergencyCase.getId()));
        assertTrue(context.getTriageService().buildPriorityQueue().isEmpty());
        assertTrue(context.getEmergencyCaseService().findActiveFloor().isEmpty());
        logout();

        loginTriage();
        context.getTriageService().assess(emergencyCase.getId(), TriagePriority.P3, null, null);
        logout();

        loginAdmin();
        assertBoardCoverage(1);
        assertTrue(context.getEmergencyCaseService().findAwaitingTriage().isEmpty());
        assertTrue(context.getTriageService().buildPriorityQueue().stream()
                .anyMatch(c -> c.caseId() == emergencyCase.getId()));
        assertTrue(context.getEmergencyCaseService().findActiveFloor().isEmpty());

        Doctor doctor = context.getDoctorService().findAvailable().stream().findFirst().orElseThrow();
        context.getAssignmentService().assign(emergencyCase.getId(), doctor.getId());
        assertBoardCoverage(1);
        assertTrue(context.getTriageService().buildPriorityQueue().isEmpty());
        assertTrue(context.getEmergencyCaseService().findActiveFloor().stream()
                .anyMatch(c -> c.getId() == emergencyCase.getId()
                        && c.getStatus() == CaseStatus.ASSIGNED));
        logout();

        loginDoctor();
        context.getAssignmentService().startTreatment(emergencyCase.getId());
        logout();

        loginAdmin();
        assertBoardCoverage(1);
        assertEquals(CaseStatus.IN_TREATMENT,
                context.getEmergencyCaseService().findActiveFloor().get(0).getStatus());
    }

    @Test
    void untaggedOpenCaseStillCountsInEsiBreakdown() {
        loginReception();
        Patient patient = context.getPatientService().register(
                "Marie", "Curie", null, null, null, null, null, null);
        EmergencyCase emergencyCase = context.getEmergencyCaseService()
                .openCase(patient.getId(), "Burn", CaseType.EMERGENCY);
        // Simulate floor leap: status moved to IN_TREATMENT without ESI (matches reported bug data)
        context.getEmergencyCaseService().updateStatus(emergencyCase.getId(), CaseStatus.IN_TREATMENT);
        logout();

        loginAdmin();
        DashboardSnapshot snap = context.getDashboardService().loadSnapshot();
        assertEquals(1, snap.openErCases());
        assertEquals(1, snap.inTreatment());

        Map<String, Long> esi = context.getDashboardService().esiBreakdown();
        assertEquals(1L, esi.get(DashboardService.ESI_UNTAGGED));

        List<EmergencyCase> active = context.getEmergencyCaseService().findActiveFloor();
        assertEquals(1, active.size());
        assertEquals(emergencyCase.getDisplayCode(), active.get(0).getDisplayCode());
        assertBoardCoverage(1);
    }

    @Test
    void erBoardsNeverDropOpenEmergencyCaseIds() {
        loginReception();
        Patient a = context.getPatientService().register("Ada", "Lovelace", null, null, null, null, null, null);
        Patient b = context.getPatientService().register("Alan", "Turing", null, null, null, null, null, null);
        EmergencyCase registered = context.getEmergencyCaseService()
                .openCase(a.getId(), "A", CaseType.EMERGENCY);
        EmergencyCase forTriage = context.getEmergencyCaseService()
                .openCase(b.getId(), "B", CaseType.EMERGENCY);
        logout();

        loginTriage();
        context.getTriageService().assess(forTriage.getId(), TriagePriority.P1, null, null);
        logout();

        loginAdmin();
        Set<Long> boardIds = boardCaseIds();
        assertTrue(boardIds.contains(registered.getId()));
        assertTrue(boardIds.contains(forTriage.getId()));
        assertEquals(2, context.getDashboardService().loadSnapshot().openErCases());
        assertBoardCoverage(2);
    }

    private void assertBoardCoverage(long expectedOpen) {
        DashboardSnapshot snap = context.getDashboardService().loadSnapshot();
        assertEquals(expectedOpen, snap.openErCases());
        long visible = context.getEmergencyCaseService().findAwaitingTriage().size()
                + context.getTriageService().buildPriorityQueue().size()
                + context.getEmergencyCaseService().findActiveFloor().size();
        assertEquals(snap.openErCases(), visible,
                "Open ER KPI must equal Needs Triage + Priority Queue + Active floor");
    }

    private Set<Long> boardCaseIds() {
        Set<Long> ids = context.getEmergencyCaseService().findAwaitingTriage().stream()
                .map(EmergencyCase::getId)
                .collect(Collectors.toSet());
        context.getTriageService().buildPriorityQueue()
                .forEach(pc -> ids.add(pc.caseId()));
        context.getEmergencyCaseService().findActiveFloor()
                .forEach(c -> ids.add(c.getId()));
        return ids;
    }
}

package com.Clinivora.service;

import com.Clinivora.model.EmergencyCase;
import com.Clinivora.model.Patient;
import com.Clinivora.model.enums.CaseType;
import com.Clinivora.model.enums.TriagePriority;
import com.Clinivora.service.dashboard.DashboardSnapshot;
import com.Clinivora.service.dashboard.PipelineRow;
import com.Clinivora.service.dashboard.WaitItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DashboardServiceTest extends ServiceTestSupport {

    @BeforeEach
    void setUp() {
        boot();
    }

    @AfterEach
    void tearDown() {
        shutdown();
    }

    @Test
    void emptyFloorSnapshotIsQuiet() {
        loginAdmin();
        DashboardSnapshot snap = context.getDashboardService().loadSnapshot();
        assertEquals(0, snap.openErCases());
        assertEquals(0, snap.awaitingTriage());
        assertTrue(snap.isQuietFloor());
        assertFalse(snap.isElevated());
        assertEquals("dashboard.floor.quiet", context.getDashboardService().floorStatusKey(snap));
        assertTrue(context.getDashboardService().longestWaits(5).isEmpty());
        assertTrue(context.getDashboardService().openPipeline(10).isEmpty());
        assertEquals(7, context.getDashboardService().arrivalsLast7Days().size());
    }

    @Test
    void openCasesFeedKpisEsiWaitsAndPipeline() {
        loginReception();
        Patient patient = context.getPatientService().register(
                "Ada", "Lovelace", null, null, null, null, null, null);
        EmergencyCase emergencyCase = context.getEmergencyCaseService()
                .openCase(patient.getId(), "Shortness of breath", CaseType.EMERGENCY);
        logout();

        loginTriage();
        context.getTriageService().assess(emergencyCase.getId(), TriagePriority.P2, "SpO2 94", "Urgent");
        logout();

        loginAdmin();
        DashboardService dashboard = context.getDashboardService();
        DashboardSnapshot snap = dashboard.loadSnapshot();
        assertTrue(snap.openErCases() >= 1);
        assertFalse(snap.isQuietFloor());

        Map<String, Long> esi = dashboard.esiBreakdown();
        assertTrue(esi.containsKey("P1"));
        assertTrue(esi.containsKey("P2"));
        assertTrue(esi.containsKey(DashboardService.ESI_UNTAGGED));
        assertEquals(1L, esi.get("P2"));
        assertEquals(0L, esi.get(DashboardService.ESI_UNTAGGED));

        List<WaitItem> waits = dashboard.longestWaits(5);
        assertFalse(waits.isEmpty());
        assertEquals(emergencyCase.getDisplayCode(), waits.get(0).caseCode());
        assertTrue(waits.get(0).patientName().contains("Ada"));
        assertEquals(TriagePriority.P2, waits.get(0).priority());

        List<PipelineRow> pipeline = dashboard.openPipeline(10);
        assertFalse(pipeline.isEmpty());
        assertEquals(emergencyCase.getId(), pipeline.get(0).caseId());
        assertTrue(pipeline.get(0).waitMinutes() >= 0);

        Map<String, Long> arrivals = dashboard.arrivalsLast7Days();
        assertEquals(7, arrivals.size());
        long totalArrivals = arrivals.values().stream().mapToLong(Long::longValue).sum();
        assertTrue(totalArrivals >= 1);

        assertTrue(dashboard.waitingLongestLabel().contains(emergencyCase.getDisplayCode()));
        Map<String, Long> legacy = dashboard.snapshot();
        assertTrue(legacy.containsKey("registeredCases") || legacy.get("inTreatment") != null
                || legacy.get("triagedCases") != null);
        assertTrue(legacy.get("patients") >= 1);
    }

    @Test
    void longestWaitsRespectsLimit() {
        loginReception();
        String[] firstNames = {"Alice", "Bob", "Carol"};
        for (String firstName : firstNames) {
            Patient patient = context.getPatientService().register(
                    firstName, "Waitman", null, null, null, null, null, null);
            context.getEmergencyCaseService().openCase(patient.getId(), "Complaint", CaseType.EMERGENCY);
        }
        logout();
        loginAdmin();
        assertEquals(2, context.getDashboardService().longestWaits(2).size());
        assertEquals(3, context.getDashboardService().openPipeline(3).size());
    }
}

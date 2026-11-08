package com.Clinivora.service.dashboard;

/**
 * Typed KPI snapshot for the operations dashboard.
 */
public record DashboardSnapshot(
        long openErCases,
        long awaitingTriage,
        long inTreatment,
        long waitingTickets,
        long availableDoctors,
        long patients,
        long triagedCases,
        long assignedCases
) {
    public boolean isQuietFloor() {
        return openErCases == 0 && waitingTickets == 0;
    }

    public boolean isElevated() {
        return awaitingTriage >= 3 || inTreatment >= 5 || waitingTickets >= 8;
    }
}

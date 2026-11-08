package com.Clinivora.service;

import com.Clinivora.model.CaseReportRow;
import com.Clinivora.model.EmergencyCase;
import com.Clinivora.model.Patient;
import com.Clinivora.model.enums.CaseStatus;
import com.Clinivora.model.enums.TicketStatus;
import com.Clinivora.model.enums.TriagePriority;
import com.Clinivora.repository.DoctorRepository;
import com.Clinivora.repository.EmergencyCaseRepository;
import com.Clinivora.repository.PatientRepository;
import com.Clinivora.repository.ServiceTicketRepository;
import com.Clinivora.repository.TriageAssessmentRepository;
import com.Clinivora.service.dashboard.DashboardSnapshot;
import com.Clinivora.service.dashboard.PipelineRow;
import com.Clinivora.service.dashboard.WaitItem;
import com.Clinivora.util.DateTimeUtil;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DashboardService {
    private final PatientRepository patientRepository;
    private final EmergencyCaseRepository caseRepository;
    private final ServiceTicketRepository ticketRepository;
    private final DoctorRepository doctorRepository;
    private final TriageAssessmentRepository triageRepository;

    public DashboardService(PatientRepository patientRepository,
                            EmergencyCaseRepository caseRepository,
                            ServiceTicketRepository ticketRepository,
                            DoctorRepository doctorRepository,
                            TriageAssessmentRepository triageRepository) {
        this.patientRepository = patientRepository;
        this.caseRepository = caseRepository;
        this.ticketRepository = ticketRepository;
        this.doctorRepository = doctorRepository;
        this.triageRepository = triageRepository;
    }

    public DashboardSnapshot loadSnapshot() {
        try {
            long registered = caseRepository.countByStatus(CaseStatus.REGISTERED);
            long triaged = caseRepository.countByStatus(CaseStatus.TRIAGED);
            long assigned = caseRepository.countByStatus(CaseStatus.ASSIGNED);
            long inTreatment = caseRepository.countByStatus(CaseStatus.IN_TREATMENT);
            long openEr = registered + triaged + assigned + inTreatment;
            return new DashboardSnapshot(
                    openEr,
                    registered,
                    inTreatment,
                    ticketRepository.countByStatus(TicketStatus.WAITING),
                    doctorRepository.findAvailable().size(),
                    patientRepository.count(),
                    triaged,
                    assigned
            );
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load dashboard snapshot", e);
        }
    }

    /** @deprecated prefer {@link #loadSnapshot()} */
    public Map<String, Long> snapshot() {
        DashboardSnapshot snap = loadSnapshot();
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("patients", snap.patients());
        stats.put("registeredCases", snap.awaitingTriage());
        stats.put("triagedCases", snap.triagedCases());
        stats.put("assignedCases", snap.assignedCases());
        stats.put("inTreatment", snap.inTreatment());
        stats.put("waitingTickets", snap.waitingTickets());
        stats.put("availableDoctors", snap.availableDoctors());
        return stats;
    }

    public static final String ESI_UNTAGGED = "UNTAGGED";

    public Map<String, Long> esiBreakdown() {
        try {
            Map<String, Long> report = new LinkedHashMap<>();
            for (TriagePriority priority : TriagePriority.values()) {
                report.put(priority.name(), 0L);
            }
            report.put(ESI_UNTAGGED, 0L);
            for (EmergencyCase emergencyCase : caseRepository.findAllOpen()) {
                var assessment = triageRepository.findByCaseId(emergencyCase.getId());
                if (assessment.isPresent()) {
                    String key = assessment.get().getPriority().name();
                    report.put(key, report.getOrDefault(key, 0L) + 1L);
                } else {
                    report.put(ESI_UNTAGGED, report.getOrDefault(ESI_UNTAGGED, 0L) + 1L);
                }
            }
            return report;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to build ESI breakdown", e);
        }
    }

    public Map<String, Long> arrivalsLast7Days() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);
        try {
            List<CaseReportRow> rows = caseRepository.findReportRowsBetween(
                    startBound(start), endBound(end), null);
            Map<String, Long> counts = new LinkedHashMap<>();
            LocalDate cursor = start;
            while (!cursor.isAfter(end)) {
                counts.put(cursor.toString(), 0L);
                cursor = cursor.plusDays(1);
            }
            for (CaseReportRow row : rows) {
                if (row.getArrivalTime() == null) {
                    continue;
                }
                String day = row.getArrivalTime().toLocalDate().toString();
                counts.put(day, counts.getOrDefault(day, 0L) + 1L);
            }
            return counts;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load arrival counts", e);
        }
    }

    public List<WaitItem> longestWaits(int limit) {
        int capped = Math.max(1, Math.min(limit, 50));
        try {
            List<WaitItem> items = new ArrayList<>();
            LocalDateTime now = DateTimeUtil.now();
            for (EmergencyCase emergencyCase : caseRepository.findAllOpen()) {
                items.add(toWaitItem(emergencyCase, now));
            }
            items.sort(Comparator.comparingLong(WaitItem::waitMinutes).reversed());
            if (items.size() > capped) {
                return List.copyOf(items.subList(0, capped));
            }
            return List.copyOf(items);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load longest waits", e);
        }
    }

    public List<PipelineRow> openPipeline(int limit) {
        int capped = Math.max(1, Math.min(limit, 100));
        try {
            List<PipelineRow> rows = new ArrayList<>();
            LocalDateTime now = DateTimeUtil.now();
            for (EmergencyCase emergencyCase : caseRepository.findAllOpen()) {
                rows.add(toPipelineRow(emergencyCase, now));
            }
            rows.sort(Comparator.comparingLong(PipelineRow::waitMinutes).reversed());
            if (rows.size() > capped) {
                return List.copyOf(rows.subList(0, capped));
            }
            return List.copyOf(rows);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load open pipeline", e);
        }
    }

    public String waitingLongestLabel() {
        List<WaitItem> waits = longestWaits(1);
        if (waits.isEmpty()) {
            return "No open cases waiting";
        }
        WaitItem item = waits.get(0);
        return item.caseCode() + " waiting " + item.waitMinutes() + " min · " + item.patientName();
    }

    public String floorStatusKey(DashboardSnapshot snapshot) {
        if (snapshot.isQuietFloor()) {
            return "dashboard.floor.quiet";
        }
        if (snapshot.isElevated()) {
            return "dashboard.floor.elevated";
        }
        return "dashboard.floor.steady";
    }

    private WaitItem toWaitItem(EmergencyCase emergencyCase, LocalDateTime now) throws SQLException {
        return new WaitItem(
                emergencyCase.getId(),
                emergencyCase.getDisplayCode(),
                patientLabel(emergencyCase.getPatientId()),
                waitMinutes(emergencyCase.getArrivalTime(), now),
                emergencyCase.getStatus(),
                priorityOf(emergencyCase.getId())
        );
    }

    private PipelineRow toPipelineRow(EmergencyCase emergencyCase, LocalDateTime now) throws SQLException {
        return new PipelineRow(
                emergencyCase.getId(),
                emergencyCase.getDisplayCode(),
                patientLabel(emergencyCase.getPatientId()),
                emergencyCase.getStatus(),
                priorityOf(emergencyCase.getId()),
                waitMinutes(emergencyCase.getArrivalTime(), now),
                emergencyCase.getChiefComplaint() == null ? "" : emergencyCase.getChiefComplaint()
        );
    }

    private TriagePriority priorityOf(long caseId) throws SQLException {
        return triageRepository.findByCaseId(caseId)
                .map(a -> a.getPriority())
                .orElse(null);
    }

    private String patientLabel(long patientId) throws SQLException {
        Optional<Patient> patient = patientRepository.findById(patientId);
        if (patient.isEmpty()) {
            return "—";
        }
        Patient p = patient.get();
        return (p.getFirstName() + " " + p.getLastName()).trim();
    }

    private static long waitMinutes(LocalDateTime arrival, LocalDateTime now) {
        if (arrival == null) {
            return 0;
        }
        return Math.max(0, Duration.between(arrival, now).toMinutes());
    }

    private static String startBound(LocalDate date) {
        return DateTimeUtil.formatDateTime(date.atStartOfDay());
    }

    private static String endBound(LocalDate date) {
        return DateTimeUtil.formatDateTime(date.atTime(LocalTime.MAX).withNano(0));
    }
}

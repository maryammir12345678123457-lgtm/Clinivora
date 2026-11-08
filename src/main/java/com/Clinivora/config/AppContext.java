package com.Clinivora.config;

import com.Clinivora.repository.AppUserRepository;
import com.Clinivora.repository.AuditLogRepository;
import com.Clinivora.repository.CaseAssignmentRepository;
import com.Clinivora.repository.CaseNoteRepository;
import com.Clinivora.repository.DoctorRepository;
import com.Clinivora.repository.EmergencyCaseRepository;
import com.Clinivora.repository.PatientEventRepository;
import com.Clinivora.repository.PatientRepository;
import com.Clinivora.repository.ServiceTicketRepository;
import com.Clinivora.repository.TriageAssessmentRepository;
import com.Clinivora.service.AccountService;
import com.Clinivora.service.AssignmentService;
import com.Clinivora.service.AuditService;
import com.Clinivora.service.AuthenticationService;
import com.Clinivora.service.AuthorizationService;
import com.Clinivora.service.CaseNoteService;
import com.Clinivora.service.DashboardService;
import com.Clinivora.service.DemoDataSeeder;
import com.Clinivora.service.DoctorService;
import com.Clinivora.service.EmergencyCaseService;
import com.Clinivora.service.NavigationService;
import com.Clinivora.service.PatientService;
import com.Clinivora.service.ReportService;
import com.Clinivora.service.RoutineQueueService;
import com.Clinivora.service.TimelineService;
import com.Clinivora.service.TriageService;
import com.Clinivora.service.UserSession;
import com.Clinivora.service.ValidationService;

public final class AppContext {
    private static AppContext instance;

    private final UserSession userSession;
    private final ValidationService validationService;
    private final AuthorizationService authorizationService;
    private final AuditService auditService;
    private final AuthenticationService authenticationService;
    private final AccountService accountService;
    private final PatientService patientService;
    private final EmergencyCaseService emergencyCaseService;
    private final CaseNoteService caseNoteService;
    private final TriageService triageService;
    private final AssignmentService assignmentService;
    private final RoutineQueueService routineQueueService;
    private final DoctorService doctorService;
    private final DashboardService dashboardService;
    private final TimelineService timelineService;
    private final ReportService reportService;
    private final NavigationService navigationService;
    private final DemoDataSeeder demoDataSeeder;

    private AppContext() {
        AppUserRepository userRepository = new AppUserRepository();
        PatientRepository patientRepository = new PatientRepository();
        DoctorRepository doctorRepository = new DoctorRepository();
        EmergencyCaseRepository caseRepository = new EmergencyCaseRepository();
        TriageAssessmentRepository triageRepository = new TriageAssessmentRepository();
        CaseAssignmentRepository assignmentRepository = new CaseAssignmentRepository();
        ServiceTicketRepository ticketRepository = new ServiceTicketRepository();
        PatientEventRepository eventRepository = new PatientEventRepository();
        AuditLogRepository auditLogRepository = new AuditLogRepository();
        CaseNoteRepository caseNoteRepository = new CaseNoteRepository();

        userSession = new UserSession();
        validationService = new ValidationService();
        authorizationService = new AuthorizationService(userSession);
        auditService = new AuditService(auditLogRepository, userSession);
        authenticationService = new AuthenticationService(
                userRepository, userSession, auditService, validationService);
        accountService = new AccountService(
                userRepository, authorizationService, validationService, auditService, userSession);
        patientService = new PatientService(
                patientRepository, eventRepository, authorizationService, validationService, auditService, userSession);
        emergencyCaseService = new EmergencyCaseService(
                caseRepository, patientRepository, eventRepository, authorizationService, auditService, userSession);
        caseNoteService = new CaseNoteService(
                caseNoteRepository, caseRepository, eventRepository, authorizationService, auditService, userSession);
        triageService = new TriageService(
                triageRepository, caseRepository, eventRepository, authorizationService, auditService, userSession);
        assignmentService = new AssignmentService(
                assignmentRepository, caseRepository, doctorRepository, eventRepository,
                authorizationService, auditService, userSession);
        routineQueueService = new RoutineQueueService(
                ticketRepository, patientRepository, caseRepository, eventRepository,
                authorizationService, auditService, userSession);
        doctorService = new DoctorService(doctorRepository, authorizationService, auditService);
        dashboardService = new DashboardService(
                patientRepository, caseRepository, ticketRepository, doctorRepository, triageRepository);
        timelineService = new TimelineService(eventRepository);
        reportService = new ReportService(caseRepository, triageRepository, assignmentRepository);
        navigationService = new NavigationService(authorizationService);
        demoDataSeeder = new DemoDataSeeder(userRepository, doctorRepository);
    }

    public static synchronized AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    public static synchronized void resetForTests() {
        instance = null;
    }

    public void bootstrap() {
        try {
            DatabaseInitializer.initialize();
            demoDataSeeder.seedIfNeeded();
        } catch (Exception e) {
            throw new IllegalStateException("Application bootstrap failed", e);
        }
    }

    public UserSession getUserSession() {
        return userSession;
    }

    public ValidationService getValidationService() {
        return validationService;
    }

    public AuthorizationService getAuthorizationService() {
        return authorizationService;
    }

    public AuditService getAuditService() {
        return auditService;
    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public AccountService getAccountService() {
        return accountService;
    }

    public PatientService getPatientService() {
        return patientService;
    }

    public EmergencyCaseService getEmergencyCaseService() {
        return emergencyCaseService;
    }

    public CaseNoteService getCaseNoteService() {
        return caseNoteService;
    }

    public TriageService getTriageService() {
        return triageService;
    }

    public AssignmentService getAssignmentService() {
        return assignmentService;
    }

    public RoutineQueueService getRoutineQueueService() {
        return routineQueueService;
    }

    public DoctorService getDoctorService() {
        return doctorService;
    }

    public DashboardService getDashboardService() {
        return dashboardService;
    }

    public TimelineService getTimelineService() {
        return timelineService;
    }

    public ReportService getReportService() {
        return reportService;
    }

    public NavigationService getNavigationService() {
        return navigationService;
    }

    public DemoDataSeeder getDemoDataSeeder() {
        return demoDataSeeder;
    }
}

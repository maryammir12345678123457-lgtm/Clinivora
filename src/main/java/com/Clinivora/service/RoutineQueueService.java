package com.Clinivora.service;

import com.Clinivora.dsa.CircularQueue;
import com.Clinivora.model.EmergencyCase;
import com.Clinivora.model.PatientEvent;
import com.Clinivora.model.ServiceTicket;
import com.Clinivora.model.enums.CaseType;
import com.Clinivora.model.enums.EventType;
import com.Clinivora.model.enums.TicketStatus;
import com.Clinivora.model.enums.UserRole;
import com.Clinivora.repository.EmergencyCaseRepository;
import com.Clinivora.repository.PatientEventRepository;
import com.Clinivora.repository.PatientRepository;
import com.Clinivora.repository.ServiceTicketRepository;
import com.Clinivora.util.DateTimeUtil;
import com.Clinivora.util.DisplayCodeFormatter;

import java.sql.SQLException;
import java.util.List;

public class RoutineQueueService {
    private final ServiceTicketRepository ticketRepository;
    private final PatientRepository patientRepository;
    private final EmergencyCaseRepository caseRepository;
    private final PatientEventRepository patientEventRepository;
    private final AuthorizationService authorizationService;
    private final AuditService auditService;
    private final UserSession session;

    public RoutineQueueService(ServiceTicketRepository ticketRepository,
                               PatientRepository patientRepository,
                               EmergencyCaseRepository caseRepository,
                               PatientEventRepository patientEventRepository,
                               AuthorizationService authorizationService,
                               AuditService auditService,
                               UserSession session) {
        this.ticketRepository = ticketRepository;
        this.patientRepository = patientRepository;
        this.caseRepository = caseRepository;
        this.patientEventRepository = patientEventRepository;
        this.authorizationService = authorizationService;
        this.auditService = auditService;
        this.session = session;
    }

    public ServiceTicket enqueue(long patientId, Long caseId) {
        authorizationService.requireRole(UserRole.ADMIN, UserRole.RECEPTION);
        try {
            if (patientRepository.findById(patientId).isEmpty()) {
                throw new IllegalArgumentException("Patient not found: " + patientId);
            }
            if (caseId != null) {
                EmergencyCase emergencyCase = caseRepository.findById(caseId)
                        .orElseThrow(() -> new IllegalArgumentException("Case not found: " + caseId));
                if (emergencyCase.getCaseType() != CaseType.ROUTINE) {
                    // allow linking anyway for walk-in routine tickets without forcing type
                }
            }

            ServiceTicket ticket = new ServiceTicket();
            ticket.setDisplayCode("TEMP");
            ticket.setPatientId(patientId);
            ticket.setCaseId(caseId);
            ticket.setQueueNumber(ticketRepository.nextQueueNumber());
            ticket.setStatus(TicketStatus.WAITING);
            ticket.setCreatedAt(DateTimeUtil.now());

            long id = ticketRepository.insert(ticket);
            String code = DisplayCodeFormatter.ticket(id);
            ticketRepository.updateDisplayCode(id, code);
            ticket.setId(id);
            ticket.setDisplayCode(code);

            PatientEvent event = new PatientEvent();
            event.setPatientId(patientId);
            event.setCaseId(caseId);
            event.setEventType(EventType.QUEUE_JOINED);
            event.setDescription("Joined routine queue as " + code);
            event.setCreatedBy(session.getUserId());
            event.setCreatedAt(DateTimeUtil.now());
            patientEventRepository.insert(event);

            auditService.record("QUEUE_JOIN", "service_ticket", id, code);
            return ticket;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to enqueue ticket", e);
        }
    }

    public ServiceTicket callNext() {
        authorizationService.requireRole(UserRole.ADMIN, UserRole.RECEPTION, UserRole.DOCTOR);
        try {
            List<ServiceTicket> waiting = ticketRepository.findByStatus(TicketStatus.WAITING);
            if (waiting.isEmpty()) {
                throw new IllegalStateException("Routine queue is empty");
            }
            CircularQueue<ServiceTicket> queue = new CircularQueue<>(Math.max(1, waiting.size()));
            for (ServiceTicket ticket : waiting) {
                queue.enqueue(ticket);
            }
            ServiceTicket next = queue.dequeue();
            ticketRepository.updateStatus(next.getId(), TicketStatus.CALLED);
            next.setStatus(TicketStatus.CALLED);
            next.setCalledAt(DateTimeUtil.now());

            PatientEvent event = new PatientEvent();
            event.setPatientId(next.getPatientId());
            event.setCaseId(next.getCaseId());
            event.setEventType(EventType.QUEUE_CALLED);
            event.setDescription("Called from routine queue: " + next.getDisplayCode());
            event.setCreatedBy(session.getUserId());
            event.setCreatedAt(DateTimeUtil.now());
            patientEventRepository.insert(event);

            auditService.record("QUEUE_CALL", "service_ticket", next.getId(), next.getDisplayCode());
            return next;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to call next ticket", e);
        }
    }

    public List<ServiceTicket> waitingTickets() {
        try {
            return ticketRepository.findByStatus(TicketStatus.WAITING);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load waiting tickets", e);
        }
    }

    public void complete(long ticketId) {
        try {
            ticketRepository.updateStatus(ticketId, TicketStatus.DONE);
            auditService.record("QUEUE_DONE", "service_ticket", ticketId, null);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to complete ticket", e);
        }
    }
}

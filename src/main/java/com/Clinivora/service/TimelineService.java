package com.Clinivora.service;

import com.Clinivora.dsa.DoublyLinkedList;
import com.Clinivora.model.PatientEvent;
import com.Clinivora.repository.PatientEventRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TimelineService {
    private final PatientEventRepository patientEventRepository;

    public TimelineService(PatientEventRepository patientEventRepository) {
        this.patientEventRepository = patientEventRepository;
    }

    public List<PatientEvent> patientTimeline(long patientId) {
        try {
            List<PatientEvent> events = patientEventRepository.findByPatientId(patientId);
            DoublyLinkedList<PatientEvent> timeline = new DoublyLinkedList<>();
            for (PatientEvent event : events) {
                timeline.addLast(event);
            }
            List<PatientEvent> result = new ArrayList<>();
            for (PatientEvent event : timeline) {
                result.add(event);
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load patient timeline", e);
        }
    }

    public List<PatientEvent> caseTimeline(long caseId) {
        try {
            return patientEventRepository.findByCaseId(caseId);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load case timeline", e);
        }
    }
}

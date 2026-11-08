package com.Clinivora.ui;

import com.Clinivora.config.AppContext;
import com.Clinivora.model.Patient;
import com.Clinivora.model.PatientEvent;
import com.Clinivora.service.NavigationService;
import com.Clinivora.ui.helper.AlertHelper;
import com.Clinivora.util.DateTimeUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.util.List;

public class PatientProfileController {
    @FXML
    private Label codeLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private Label detailsLabel;
    @FXML
    private ListView<String> timelineList;

    @FXML
    private void initialize() {
        Long patientId = AppContext.getInstance().getNavigationService().getSelectedPatientId();
        if (patientId == null) {
            codeLabel.setText("No patient selected");
            return;
        }
        Patient patient = AppContext.getInstance().getPatientService().findById(patientId).orElse(null);
        if (patient == null) {
            AlertHelper.showError("Patient missing", "Patient #" + patientId + " was not found.");
            return;
        }
        codeLabel.setText(patient.getDisplayCode());
        nameLabel.setText(patient.getFullName());
        detailsLabel.setText("Phone: " + nullSafe(patient.getPhone())
                + "  ·  Gender: " + nullSafe(patient.getGender())
                + "  ·  DOB: " + (patient.getDateOfBirth() == null ? "-" : patient.getDateOfBirth())
                + "\nAddress: " + nullSafe(patient.getAddress())
                + "\nEmergency contact: " + nullSafe(patient.getEmergencyContact()));

        List<PatientEvent> events = AppContext.getInstance().getTimelineService().patientTimeline(patientId);
        timelineList.setItems(FXCollections.observableArrayList(
                events.stream()
                        .map(e -> DateTimeUtil.formatDisplay(e.getCreatedAt())
                                + "  ·  " + e.getEventType()
                                + "  ·  " + nullSafe(e.getDescription()))
                        .toList()));
    }

    @FXML
    private void onBack() {
        AppContext.getInstance().getNavigationService().goTo(NavigationService.View.PATIENTS);
    }

    private static String nullSafe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}

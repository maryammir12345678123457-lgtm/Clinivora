package com.Clinivora.ui;

import com.Clinivora.config.AppContext;
import com.Clinivora.model.CaseNote;
import com.Clinivora.model.Doctor;
import com.Clinivora.model.EmergencyCase;
import com.Clinivora.model.PatientEvent;
import com.Clinivora.model.TriageAssessment;
import com.Clinivora.model.enums.TriagePriority;
import com.Clinivora.service.AuthorizationService;
import com.Clinivora.service.NavigationService;
import com.Clinivora.ui.helper.AlertHelper;
import com.Clinivora.util.DateTimeUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.util.List;

public class CaseDetailController {
    @FXML
    private Label caseCodeLabel;
    @FXML
    private Label statusChip;
    @FXML
    private Label priorityChip;
    @FXML
    private Label caseMetaLabel;
    @FXML
    private Label triageLabel;
    @FXML
    private HBox complaintBox;
    @FXML
    private TextField complaintField;
    @FXML
    private Button saveComplaintButton;
    @FXML
    private ComboBox<Doctor> doctorBox;
    @FXML
    private ListView<String> timelineList;
    @FXML
    private TextArea noteArea;
    @FXML
    private Button addNoteButton;
    @FXML
    private ListView<String> notesList;

    private EmergencyCase emergencyCase;

    @FXML
    private void initialize() {
        AuthorizationService auth = AppContext.getInstance().getAuthorizationService();
        complaintBox.setVisible(auth.canEditCaseComplaint());
        complaintBox.setManaged(complaintBox.isVisible());
        addNoteButton.setDisable(!auth.canAddClinicalNotes());
        noteArea.setDisable(!auth.canAddClinicalNotes());

        doctorBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Doctor doctor) {
                return doctor == null ? "" : doctor.getDisplayCode() + " · " + doctor.getFullName()
                        + (doctor.isAvailable() ? " (available)" : " (busy)");
            }

            @Override
            public Doctor fromString(String string) {
                return null;
            }
        });
        reload();
    }

    private void reload() {
        Long caseId = AppContext.getInstance().getNavigationService().getSelectedCaseId();
        if (caseId == null) {
            caseCodeLabel.setText("No case selected");
            return;
        }
        emergencyCase = AppContext.getInstance().getEmergencyCaseService().findById(caseId).orElse(null);
        if (emergencyCase == null) {
            AlertHelper.showError("Case missing", "Case #" + caseId + " was not found.");
            return;
        }
        caseCodeLabel.setText(emergencyCase.getDisplayCode());
        statusChip.setText(emergencyCase.getStatus().name());
        statusChip.getStyleClass().setAll("status-chip", "chip-neutral");
        caseMetaLabel.setText("Patient #" + emergencyCase.getPatientId()
                + "  ·  Type: " + emergencyCase.getCaseType()
                + "\nArrival: " + DateTimeUtil.formatDisplay(emergencyCase.getArrivalTime()));
        complaintField.setText(emergencyCase.getChiefComplaint());

        TriageAssessment assessment = AppContext.getInstance().getTriageService()
                .findByCaseId(caseId).orElse(null);
        if (assessment == null) {
            triageLabel.setText("Not yet triaged");
            priorityChip.setText("");
            priorityChip.getStyleClass().clear();
        } else {
            triageLabel.setText((assessment.getVitalSigns() == null ? "No vitals" : assessment.getVitalSigns())
                    + " · " + (assessment.getNotes() == null ? "" : assessment.getNotes()));
            applyPriorityChip(assessment.getPriority());
        }

        doctorBox.setItems(FXCollections.observableArrayList(
                AppContext.getInstance().getDoctorService().findAll()));

        List<PatientEvent> events = AppContext.getInstance().getTimelineService().caseTimeline(caseId);
        timelineList.setItems(FXCollections.observableArrayList(
                events.stream()
                        .map(e -> DateTimeUtil.formatDisplay(e.getCreatedAt())
                                + "  ·  " + e.getEventType()
                                + "  ·  " + (e.getDescription() == null ? "" : e.getDescription()))
                        .toList()));

        List<CaseNote> notes = AppContext.getInstance().getCaseNoteService().listNotes(caseId);
        notesList.setItems(FXCollections.observableArrayList(
                notes.stream()
                        .map(n -> DateTimeUtil.formatDisplay(n.getCreatedAt()) + " · " + n.getBody())
                        .toList()));
        if (notes.isEmpty()) {
            notesList.setPlaceholder(new Label("No clinical notes yet."));
        }
    }

    private void applyPriorityChip(TriagePriority priority) {
        priorityChip.setText(priority.name());
        priorityChip.getStyleClass().setAll(switch (priority) {
            case P1 -> "esi-p1";
            case P2 -> "esi-p2";
            case P3 -> "esi-p3";
            case P4 -> "esi-p4";
            case P5 -> "esi-p5";
        });
    }

    @FXML
    private void onBack() {
        AppContext.getInstance().getNavigationService().goTo(NavigationService.View.EMERGENCY_ROOM);
    }

    @FXML
    private void onSaveComplaint() {
        if (emergencyCase == null) {
            return;
        }
        try {
            AppContext.getInstance().getEmergencyCaseService()
                    .updateComplaint(emergencyCase.getId(), complaintField.getText());
            AlertHelper.showSuccess("Complaint updated", emergencyCase.getDisplayCode());
            reload();
        } catch (Exception ex) {
            AlertHelper.showError("Update failed", ex.getMessage());
        }
    }

    @FXML
    private void onAddNote() {
        if (emergencyCase == null) {
            return;
        }
        try {
            AppContext.getInstance().getCaseNoteService()
                    .addNote(emergencyCase.getId(), noteArea.getText());
            noteArea.clear();
            AlertHelper.showSuccess("Note added", "Clinical note saved.");
            reload();
        } catch (Exception ex) {
            AlertHelper.showError("Note failed", ex.getMessage());
        }
    }

    @FXML
    private void onAssign() {
        Doctor doctor = doctorBox.getValue();
        if (doctor == null || emergencyCase == null) {
            AlertHelper.showInfo("Select a doctor", "Choose a doctor to assign.");
            return;
        }
        if (!AlertHelper.confirmAssign()) {
            return;
        }
        try {
            AppContext.getInstance().getAssignmentService().assign(emergencyCase.getId(), doctor.getId());
            AlertHelper.showSuccess("Assigned", doctor.getFullName() + " → " + emergencyCase.getDisplayCode());
            reload();
        } catch (Exception ex) {
            AlertHelper.showError("Assign failed", ex.getMessage());
        }
    }

    @FXML
    private void onStartTreatment() {
        if (emergencyCase == null || !AlertHelper.confirmStartTreatment()) {
            return;
        }
        try {
            AppContext.getInstance().getAssignmentService().startTreatment(emergencyCase.getId());
            AlertHelper.showSuccess("Treatment started", emergencyCase.getDisplayCode());
            reload();
        } catch (Exception ex) {
            AlertHelper.showError("Unable to start treatment", ex.getMessage());
        }
    }

    @FXML
    private void onCloseCase() {
        if (emergencyCase == null || !AlertHelper.confirmCloseCase()) {
            return;
        }
        try {
            AppContext.getInstance().getAssignmentService().complete(emergencyCase.getId());
            AlertHelper.showSuccess("Case closed", emergencyCase.getDisplayCode());
            reload();
        } catch (Exception ex) {
            AlertHelper.showError("Unable to close case", ex.getMessage());
        }
    }
}

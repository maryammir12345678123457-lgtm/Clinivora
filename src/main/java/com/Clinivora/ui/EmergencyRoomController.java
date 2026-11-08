package com.Clinivora.ui;

import com.Clinivora.config.AppContext;
import com.Clinivora.model.EmergencyCase;
import com.Clinivora.model.enums.TriagePriority;
import com.Clinivora.service.NavigationService;
import com.Clinivora.service.TriageService;
import com.Clinivora.ui.helper.AlertHelper;
import com.Clinivora.ui.i18n.I18n;
import com.Clinivora.util.DateTimeUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class EmergencyRoomController {
    @FXML
    private Label floorSummaryLabel;
    @FXML
    private TableView<EmergencyCase> needsTriageTable;
    @FXML
    private TableColumn<EmergencyCase, String> triageCodeCol;
    @FXML
    private TableColumn<EmergencyCase, String> triagePatientCol;
    @FXML
    private TableColumn<EmergencyCase, String> triageArrivalCol;
    @FXML
    private TableColumn<EmergencyCase, String> triageComplaintCol;
    @FXML
    private TableView<TriageService.PriorityCase> priorityTable;
    @FXML
    private TableColumn<TriageService.PriorityCase, String> prioCodeCol;
    @FXML
    private TableColumn<TriageService.PriorityCase, String> prioPriorityCol;
    @FXML
    private TableColumn<TriageService.PriorityCase, String> prioArrivalCol;
    @FXML
    private TableView<EmergencyCase> activeFloorTable;
    @FXML
    private TableColumn<EmergencyCase, String> activeCodeCol;
    @FXML
    private TableColumn<EmergencyCase, String> activePatientCol;
    @FXML
    private TableColumn<EmergencyCase, String> activeStatusCol;
    @FXML
    private TableColumn<EmergencyCase, String> activeArrivalCol;
    @FXML
    private VBox triageOverlay;
    @FXML
    private Label triageCaseLabel;
    @FXML
    private ComboBox<TriagePriority> priorityBox;
    @FXML
    private ComboBox<TriagePriority> queuePriorityBox;
    @FXML
    private ComboBox<TriagePriority> floorPriorityBox;
    @FXML
    private TextField vitalsField;
    @FXML
    private TextArea notesArea;

    private EmergencyCase triageTarget;

    @FXML
    private void initialize() {
        triageCodeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDisplayCode()));
        triagePatientCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getPatientId())));
        triageArrivalCol.setCellValueFactory(c -> new SimpleStringProperty(
                DateTimeUtil.formatDisplay(c.getValue().getArrivalTime())));
        triageComplaintCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getChiefComplaint() == null ? "" : c.getValue().getChiefComplaint()));

        prioCodeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().displayCode()));
        prioPriorityCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().priority().name()));
        prioPriorityCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label chip = new Label(item);
                chip.getStyleClass().setAll(switch (item) {
                    case "P1" -> "esi-p1";
                    case "P2" -> "esi-p2";
                    case "P3" -> "esi-p3";
                    case "P4" -> "esi-p4";
                    case "P5" -> "esi-p5";
                    default -> "chip-neutral";
                });
                setGraphic(chip);
                setText(null);
            }
        });
        prioArrivalCol.setCellValueFactory(c -> new SimpleStringProperty(
                DateTimeUtil.formatDisplay(c.getValue().arrivalTime())));

        activeCodeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDisplayCode()));
        activePatientCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getPatientId())));
        activeStatusCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStatus() == null ? "—" : c.getValue().getStatus().name()));
        activeArrivalCol.setCellValueFactory(c -> new SimpleStringProperty(
                DateTimeUtil.formatDisplay(c.getValue().getArrivalTime())));

        needsTriageTable.setPlaceholder(new Label("No cases awaiting triage."));
        priorityTable.setPlaceholder(new Label("Priority queue is empty."));
        activeFloorTable.setPlaceholder(new Label("No cases on the active floor."));
        priorityBox.setItems(FXCollections.observableArrayList(TriagePriority.values()));
        queuePriorityBox.setItems(FXCollections.observableArrayList(TriagePriority.values()));
        floorPriorityBox.setItems(FXCollections.observableArrayList(TriagePriority.values()));
        queuePriorityBox.setValue(TriagePriority.P3);
        floorPriorityBox.setValue(TriagePriority.P3);
        onRefresh();
    }

    @FXML
    private void onRefresh() {
        var ctx = AppContext.getInstance();
        var awaiting = ctx.getEmergencyCaseService().findAwaitingTriage();
        var priority = ctx.getTriageService().buildPriorityQueue();
        var active = ctx.getEmergencyCaseService().findActiveFloor();
        needsTriageTable.setItems(FXCollections.observableArrayList(awaiting));
        priorityTable.setItems(FXCollections.observableArrayList(priority));
        activeFloorTable.setItems(FXCollections.observableArrayList(active));

        long open = ctx.getDashboardService().loadSnapshot().openErCases();
        long visible = awaiting.size() + priority.size() + active.size();
        floorSummaryLabel.setText(I18n.getInstance().t(
                "er.floor.summary", open, awaiting.size(), priority.size(), active.size(), visible));
    }

    @FXML
    private void onOpenTriage() {
        triageTarget = needsTriageTable.getSelectionModel().getSelectedItem();
        if (triageTarget == null) {
            AlertHelper.showInfo("Select a case", "Choose a case that needs triage.");
            return;
        }
        triageCaseLabel.setText(triageTarget.getDisplayCode());
        priorityBox.setValue(TriagePriority.P3);
        vitalsField.clear();
        notesArea.clear();
        triageOverlay.setVisible(true);
        triageOverlay.setManaged(true);
    }

    @FXML
    private void onCloseTriage() {
        triageOverlay.setVisible(false);
        triageOverlay.setManaged(false);
    }

    @FXML
    private void onSaveTriage() {
        try {
            AppContext.getInstance().getTriageService().assess(
                    triageTarget.getId(),
                    priorityBox.getValue(),
                    vitalsField.getText(),
                    notesArea.getText());
            onCloseTriage();
            onRefresh();
            AlertHelper.showSuccess("Triage saved", triageTarget.getDisplayCode() + " → " + priorityBox.getValue());
        } catch (Exception ex) {
            AlertHelper.showError("Triage failed", ex.getMessage());
        }
    }

    @FXML
    private void onCancelCase() {
        EmergencyCase selected = needsTriageTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showInfo("Select a case", "Choose a case to cancel.");
            return;
        }
        if (!AlertHelper.confirmCancelCase()) {
            return;
        }
        try {
            AppContext.getInstance().getEmergencyCaseService().cancelCase(selected.getId());
            onRefresh();
            AlertHelper.showSuccess("Case cancelled", selected.getDisplayCode());
        } catch (Exception ex) {
            AlertHelper.showError("Cancel failed", ex.getMessage());
        }
    }

    @FXML
    private void onOpenDetailFromTriage() {
        EmergencyCase selected = needsTriageTable.getSelectionModel().getSelectedItem();
        openDetail(selected == null ? null : selected.getId());
    }

    @FXML
    private void onOpenDetailFromPriority() {
        TriageService.PriorityCase selected = priorityTable.getSelectionModel().getSelectedItem();
        openDetail(selected == null ? null : selected.caseId());
    }

    @FXML
    private void onSetQueuePriority() {
        TriageService.PriorityCase selected = priorityTable.getSelectionModel().getSelectedItem();
        if (selected == null || queuePriorityBox.getValue() == null) {
            AlertHelper.showInfo("Select a case", "Choose a priority-queue case and an ESI level.");
            return;
        }
        try {
            AppContext.getInstance().getTriageService()
                    .assess(selected.caseId(), queuePriorityBox.getValue(), null, "Priority updated");
            onRefresh();
        } catch (Exception ex) {
            AlertHelper.showError("Priority update failed", ex.getMessage());
        }
    }

    @FXML
    private void onSendToFloor() {
        TriageService.PriorityCase selected = priorityTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showInfo("Select a case", "Choose a case in the priority queue.");
            return;
        }
        var doctors = AppContext.getInstance().getDoctorService().findAvailable();
        if (doctors.isEmpty()) {
            AlertHelper.showInfo("No doctor", "No available doctor to assign.");
            return;
        }
        try {
            AppContext.getInstance().getAssignmentService()
                    .assign(selected.caseId(), doctors.get(0).getId());
            onRefresh();
        } catch (Exception ex) {
            AlertHelper.showError("Send to floor failed", ex.getMessage());
        }
    }

    @FXML
    private void onUpdateFloorPriority() {
        EmergencyCase selected = activeFloorTable.getSelectionModel().getSelectedItem();
        if (selected == null || floorPriorityBox.getValue() == null) {
            AlertHelper.showInfo("Select a case", "Choose an active-floor case and an ESI level.");
            return;
        }
        try {
            AppContext.getInstance().getTriageService()
                    .assess(selected.getId(), floorPriorityBox.getValue(), null, "ESI updated on floor");
            onRefresh();
        } catch (Exception ex) {
            AlertHelper.showError("ESI update failed", ex.getMessage());
        }
    }

    @FXML
    private void onStartTreatment() {
        EmergencyCase selected = activeFloorTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showInfo("Select a case", "Choose a case on the active floor.");
            return;
        }
        try {
            AppContext.getInstance().getAssignmentService().startTreatment(selected.getId());
            onRefresh();
        } catch (Exception ex) {
            AlertHelper.showError("Start treatment failed", ex.getMessage());
        }
    }

    @FXML
    private void onDischarge() {
        EmergencyCase selected = activeFloorTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showInfo("Select a case", "Choose a case to discharge.");
            return;
        }
        try {
            AppContext.getInstance().getAssignmentService().complete(selected.getId());
            onRefresh();
        } catch (Exception ex) {
            AlertHelper.showError("Discharge failed", ex.getMessage());
        }
    }

    @FXML
    private void onOpenDetailFromActive() {
        EmergencyCase selected = activeFloorTable.getSelectionModel().getSelectedItem();
        openDetail(selected == null ? null : selected.getId());
    }

    private void openDetail(Long caseId) {
        if (caseId == null) {
            AlertHelper.showInfo("Select a case", "Choose a case first.");
            return;
        }
        NavigationService nav = AppContext.getInstance().getNavigationService();
        nav.setSelectedCaseId(caseId);
        nav.goTo(NavigationService.View.CASE_DETAIL);
    }
}

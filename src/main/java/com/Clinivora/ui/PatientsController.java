package com.Clinivora.ui;

import com.Clinivora.config.AppContext;
import com.Clinivora.dsa.ArrayStack;
import com.Clinivora.model.Patient;
import com.Clinivora.model.enums.CaseType;
import com.Clinivora.model.enums.UserRole;
import com.Clinivora.service.AuthorizationService;
import com.Clinivora.service.NavigationService;
import com.Clinivora.ui.helper.AlertHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

public class PatientsController {
    private record FormSnapshot(String first, String last, LocalDate dob, String phone,
                                String gender, String nationalId, String address, String emergency) {
    }

    @FXML
    private TextField searchField;
    @FXML
    private TableView<Patient> patientTable;
    @FXML
    private TableColumn<Patient, String> codeCol;
    @FXML
    private TableColumn<Patient, String> nameCol;
    @FXML
    private TableColumn<Patient, String> phoneCol;
    @FXML
    private TableColumn<Patient, String> genderCol;
    @FXML
    private TableColumn<Patient, String> dobCol;
    @FXML
    private TableColumn<Patient, String> statusCol;
    @FXML
    private VBox formOverlay;
    @FXML
    private Label formTitle;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private DatePicker dobPicker;
    @FXML
    private TextField phoneField;
    @FXML
    private ComboBox<String> genderBox;
    @FXML
    private TextField nationalIdField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField emergencyField;
    @FXML
    private Label formMessage;
    @FXML
    private Button newButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button restoreButton;
    @FXML
    private Button openCaseButton;
    @FXML
    private CheckBox showInactiveBox;

    private final ArrayStack<FormSnapshot> undoStack = new ArrayStack<>();
    private boolean capturing = true;
    private Long editingPatientId;

    @FXML
    private void initialize() {
        AuthorizationService auth = AppContext.getInstance().getAuthorizationService();
        newButton.setVisible(auth.canRegisterPatients());
        newButton.setManaged(newButton.isVisible());
        editButton.setVisible(auth.canEditPatients());
        editButton.setManaged(editButton.isVisible());
        deleteButton.setVisible(auth.canSoftDeletePatients());
        deleteButton.setManaged(deleteButton.isVisible());
        restoreButton.setVisible(auth.canRestorePatients());
        restoreButton.setManaged(restoreButton.isVisible());
        openCaseButton.setVisible(auth.hasRole(UserRole.ADMIN, UserRole.RECEPTION, UserRole.TRIAGE));
        openCaseButton.setManaged(openCaseButton.isVisible());
        showInactiveBox.setVisible(auth.hasRole(UserRole.ADMIN));
        showInactiveBox.setManaged(showInactiveBox.isVisible());

        codeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDisplayCode()));
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        phoneCol.setCellValueFactory(c -> new SimpleStringProperty(nullToEmpty(c.getValue().getPhone())));
        genderCol.setCellValueFactory(c -> new SimpleStringProperty(nullToEmpty(c.getValue().getGender())));
        dobCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDateOfBirth() == null ? "" : c.getValue().getDateOfBirth().toString()));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActive() ? "Active" : "Inactive"));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label chip = new Label(item);
                chip.getStyleClass().addAll("status-chip",
                        "Active".equals(item) ? "chip-success" : "chip-danger");
                setGraphic(chip);
                setText(null);
            }
        });
        genderBox.setItems(FXCollections.observableArrayList("Female", "Male", "Other", "Unspecified"));
        wireUndoTracking(firstNameField, lastNameField, phoneField, nationalIdField, addressField, emergencyField);
        dobPicker.valueProperty().addListener((o, a, b) -> pushSnapshot());
        genderBox.valueProperty().addListener((o, a, b) -> pushSnapshot());
        refresh();
    }

    private void wireUndoTracking(TextField... fields) {
        for (TextField field : fields) {
            field.textProperty().addListener((o, a, b) -> pushSnapshot());
        }
    }

    private void pushSnapshot() {
        if (!capturing || !formOverlay.isVisible()) {
            return;
        }
        undoStack.push(capture());
    }

    private FormSnapshot capture() {
        return new FormSnapshot(
                firstNameField.getText(),
                lastNameField.getText(),
                dobPicker.getValue(),
                phoneField.getText(),
                genderBox.getValue(),
                nationalIdField.getText(),
                addressField.getText(),
                emergencyField.getText());
    }

    private void apply(FormSnapshot snapshot) {
        capturing = false;
        firstNameField.setText(snapshot.first());
        lastNameField.setText(snapshot.last());
        dobPicker.setValue(snapshot.dob());
        phoneField.setText(snapshot.phone());
        genderBox.setValue(snapshot.gender());
        nationalIdField.setText(snapshot.nationalId());
        addressField.setText(snapshot.address());
        emergencyField.setText(snapshot.emergency());
        capturing = true;
    }

    @FXML
    private void onSearch() {
        refresh();
    }

    private void refresh() {
        boolean includeInactive = showInactiveBox.isVisible() && showInactiveBox.isSelected();
        List<Patient> patients = AppContext.getInstance().getPatientService()
                .search(searchField.getText(), includeInactive);
        patientTable.setItems(FXCollections.observableArrayList(patients));
    }

    @FXML
    private void onShowForm() {
        editingPatientId = null;
        formTitle.setText("Register patient");
        clearForm();
        formMessage.setText("Edits are tracked on ArrayStack for Undo.");
        showForm();
    }

    @FXML
    private void onEdit() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showInfo("Select a patient", "Choose a patient row first.");
            return;
        }
        editingPatientId = selected.getId();
        formTitle.setText("Edit patient · " + selected.getDisplayCode());
        capturing = false;
        firstNameField.setText(selected.getFirstName());
        lastNameField.setText(selected.getLastName());
        dobPicker.setValue(selected.getDateOfBirth());
        phoneField.setText(selected.getPhone());
        genderBox.setValue(selected.getGender());
        nationalIdField.setText(selected.getNationalId());
        addressField.setText(selected.getAddress());
        emergencyField.setText(selected.getEmergencyContact());
        capturing = true;
        undoStack.clear();
        undoStack.push(capture());
        formMessage.setText("Update demographics, then Save.");
        showForm();
    }

    private void clearForm() {
        undoStack.clear();
        capturing = false;
        firstNameField.clear();
        lastNameField.clear();
        dobPicker.setValue(null);
        phoneField.clear();
        genderBox.setValue(null);
        nationalIdField.clear();
        addressField.clear();
        emergencyField.clear();
        capturing = true;
        undoStack.push(capture());
    }

    private void showForm() {
        formOverlay.setVisible(true);
        formOverlay.setManaged(true);
    }

    @FXML
    private void onHideForm() {
        formOverlay.setVisible(false);
        formOverlay.setManaged(false);
        editingPatientId = null;
    }

    @FXML
    private void onUndo() {
        if (undoStack.size() <= 1) {
            formMessage.setText("Nothing to undo.");
            return;
        }
        undoStack.pop();
        apply(undoStack.peek());
        formMessage.setText("Restored previous form state.");
    }

    @FXML
    private void onSave() {
        try {
            if (editingPatientId == null) {
                Patient patient = AppContext.getInstance().getPatientService().register(
                        firstNameField.getText(), lastNameField.getText(), dobPicker.getValue(),
                        phoneField.getText(), genderBox.getValue(), nationalIdField.getText(),
                        addressField.getText(), emergencyField.getText());
                onHideForm();
                refresh();
                AlertHelper.showSuccess("Patient registered", patient.getDisplayCode() + " · " + patient.getFullName());
            } else {
                Patient patient = AppContext.getInstance().getPatientService().update(
                        editingPatientId,
                        firstNameField.getText(), lastNameField.getText(), dobPicker.getValue(),
                        phoneField.getText(), genderBox.getValue(), nationalIdField.getText(),
                        addressField.getText(), emergencyField.getText());
                onHideForm();
                refresh();
                AlertHelper.showSuccess("Patient updated", patient.getDisplayCode());
            }
        } catch (Exception ex) {
            AlertHelper.showError("Save failed", ex.getMessage());
        }
    }

    @FXML
    private void onDelete() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showInfo("Select a patient", "Choose a patient row first.");
            return;
        }
        if (!AlertHelper.confirmSoftDelete(selected.getDisplayCode())) {
            return;
        }
        try {
            AppContext.getInstance().getPatientService().softDelete(selected.getId());
            refresh();
            AlertHelper.showSuccess("Patient deactivated", selected.getDisplayCode());
        } catch (Exception ex) {
            AlertHelper.showError("Delete failed", ex.getMessage());
        }
    }

    @FXML
    private void onRestore() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showInfo("Select a patient", "Choose an inactive patient row first.");
            return;
        }
        if (!AlertHelper.confirmRestore(selected.getDisplayCode())) {
            return;
        }
        try {
            AppContext.getInstance().getPatientService().restore(selected.getId());
            refresh();
            AlertHelper.showSuccess("Patient restored", selected.getDisplayCode());
        } catch (Exception ex) {
            AlertHelper.showError("Restore failed", ex.getMessage());
        }
    }

    @FXML
    private void onOpenProfile() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showInfo("Select a patient", "Choose a patient row first.");
            return;
        }
        NavigationService nav = AppContext.getInstance().getNavigationService();
        nav.setSelectedPatientId(selected.getId());
        nav.goTo(NavigationService.View.PATIENT_PROFILE);
    }

    @FXML
    private void onOpenEmergency() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showInfo("Select a patient", "Choose a patient row first.");
            return;
        }
        try {
            var emergencyCase = AppContext.getInstance().getEmergencyCaseService()
                    .openCase(selected.getId(), "Emergency intake", CaseType.EMERGENCY);
            AlertHelper.showSuccess("Case opened", emergencyCase.getDisplayCode());
            NavigationService nav = AppContext.getInstance().getNavigationService();
            nav.setSelectedCaseId(emergencyCase.getId());
            nav.goTo(NavigationService.View.EMERGENCY_ROOM);
        } catch (Exception ex) {
            AlertHelper.showError("Unable to open case", ex.getMessage());
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}

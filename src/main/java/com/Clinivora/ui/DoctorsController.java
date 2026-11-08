package com.Clinivora.ui;

import com.Clinivora.config.AppContext;
import com.Clinivora.model.Doctor;
import com.Clinivora.model.enums.UserRole;
import com.Clinivora.ui.helper.AlertHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class DoctorsController {
    @FXML
    private TextField nameField;
    @FXML
    private TextField specialtyField;
    @FXML
    private Button addButton;
    @FXML
    private Button deactivateButton;
    @FXML
    private Button activateButton;
    @FXML
    private CheckBox showInactiveBox;
    @FXML
    private TableView<Doctor> doctorTable;
    @FXML
    private TableColumn<Doctor, String> codeCol;
    @FXML
    private TableColumn<Doctor, String> nameCol;
    @FXML
    private TableColumn<Doctor, String> specialtyCol;
    @FXML
    private TableColumn<Doctor, String> availableCol;
    @FXML
    private TableColumn<Doctor, String> activeCol;

    @FXML
    private void initialize() {
        codeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDisplayCode()));
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        specialtyCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getSpecialty() == null ? "" : c.getValue().getSpecialty()));
        availableCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().isAvailable() ? "Yes" : "No"));
        activeCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().isActive() ? "Active" : "Inactive"));
        activeCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label chip = new Label(item);
                chip.getStyleClass().addAll("status-chip",
                        "Active".equals(item) ? "chip-success" : "chip-danger");
                setGraphic(chip);
                setText(null);
            }
        });

        boolean admin = AppContext.getInstance().getAuthorizationService().hasRole(UserRole.ADMIN);
        addButton.setDisable(!admin);
        nameField.setDisable(!admin);
        specialtyField.setDisable(!admin);
        deactivateButton.setVisible(admin);
        deactivateButton.setManaged(admin);
        activateButton.setVisible(admin);
        activateButton.setManaged(admin);
        showInactiveBox.setVisible(admin);
        showInactiveBox.setManaged(admin);
        onRefresh();
    }

    @FXML
    private void onRefresh() {
        boolean includeInactive = showInactiveBox.isVisible() && showInactiveBox.isSelected();
        doctorTable.setItems(FXCollections.observableArrayList(
                AppContext.getInstance().getDoctorService().findAll(includeInactive)));
    }

    @FXML
    private void onAdd() {
        try {
            Doctor doctor = AppContext.getInstance().getDoctorService()
                    .create(nameField.getText(), specialtyField.getText(), null);
            AlertHelper.showSuccess("Doctor added", doctor.getDisplayCode());
            nameField.clear();
            specialtyField.clear();
            onRefresh();
        } catch (Exception ex) {
            AlertHelper.showError("Unable to add doctor", ex.getMessage());
        }
    }

    @FXML
    private void onToggle() {
        Doctor selected = doctorTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showInfo("Select a doctor", "Choose a doctor row first.");
            return;
        }
        try {
            AppContext.getInstance().getDoctorService()
                    .setAvailable(selected.getId(), !selected.isAvailable());
            onRefresh();
        } catch (Exception ex) {
            AlertHelper.showError("Update failed", ex.getMessage());
        }
    }

    @FXML
    private void onDeactivate() {
        Doctor selected = doctorTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showInfo("Select a doctor", "Choose a doctor row first.");
            return;
        }
        if (!AlertHelper.confirmDeactivate(selected.getDisplayCode())) {
            return;
        }
        try {
            AppContext.getInstance().getDoctorService().setActive(selected.getId(), false);
            onRefresh();
            AlertHelper.showSuccess("Doctor deactivated", selected.getDisplayCode());
        } catch (Exception ex) {
            AlertHelper.showError("Deactivate failed", ex.getMessage());
        }
    }

    @FXML
    private void onActivate() {
        Doctor selected = doctorTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showInfo("Select a doctor", "Choose a doctor row first.");
            return;
        }
        try {
            AppContext.getInstance().getDoctorService().setActive(selected.getId(), true);
            onRefresh();
            AlertHelper.showSuccess("Doctor activated", selected.getDisplayCode());
        } catch (Exception ex) {
            AlertHelper.showError("Activate failed", ex.getMessage());
        }
    }
}

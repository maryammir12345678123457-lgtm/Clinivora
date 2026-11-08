package com.Clinivora.ui;

import com.Clinivora.config.AppContext;
import com.Clinivora.model.Patient;
import com.Clinivora.model.ServiceTicket;
import com.Clinivora.ui.helper.AlertHelper;
import com.Clinivora.util.DateTimeUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.StringConverter;

public class RoutineQueueController {
    @FXML
    private ComboBox<Patient> patientBox;
    @FXML
    private TableView<ServiceTicket> ticketTable;
    @FXML
    private TableColumn<ServiceTicket, String> codeCol;
    @FXML
    private TableColumn<ServiceTicket, String> queueCol;
    @FXML
    private TableColumn<ServiceTicket, String> patientCol;
    @FXML
    private TableColumn<ServiceTicket, String> statusCol;
    @FXML
    private TableColumn<ServiceTicket, String> createdCol;

    @FXML
    private void initialize() {
        codeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDisplayCode()));
        queueCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getQueueNumber() == null ? "" : String.valueOf(c.getValue().getQueueNumber())));
        patientCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getPatientId())));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().name()));
        createdCol.setCellValueFactory(c -> new SimpleStringProperty(
                DateTimeUtil.formatDisplay(c.getValue().getCreatedAt())));

        patientBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Patient patient) {
                return patient == null ? "" : patient.getDisplayCode() + " · " + patient.getFullName();
            }

            @Override
            public Patient fromString(String string) {
                return null;
            }
        });
        patientBox.setItems(FXCollections.observableArrayList(
                AppContext.getInstance().getPatientService().findAll()));
        onRefresh();
    }

    @FXML
    private void onRefresh() {
        ticketTable.setItems(FXCollections.observableArrayList(
                AppContext.getInstance().getRoutineQueueService().waitingTickets()));
    }

    @FXML
    private void onEnqueue() {
        Patient patient = patientBox.getValue();
        if (patient == null) {
            AlertHelper.showInfo("Select a patient", "Choose a patient to enqueue.");
            return;
        }
        try {
            ServiceTicket ticket = AppContext.getInstance().getRoutineQueueService().enqueue(patient.getId(), null);
            AlertHelper.showSuccess("Enqueued", ticket.getDisplayCode() + " (#" + ticket.getQueueNumber() + ")");
            onRefresh();
        } catch (Exception ex) {
            AlertHelper.showError("Enqueue failed", ex.getMessage());
        }
    }

    @FXML
    private void onServeNext() {
        if (!AlertHelper.confirmServeNext()) {
            return;
        }
        try {
            ServiceTicket next = AppContext.getInstance().getRoutineQueueService().callNext();
            AlertHelper.showSuccess("Now serving", next.getDisplayCode());
            onRefresh();
        } catch (Exception ex) {
            AlertHelper.showError("Queue empty", ex.getMessage());
        }
    }
}

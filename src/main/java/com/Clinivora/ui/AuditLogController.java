package com.Clinivora.ui;

import com.Clinivora.config.AppContext;
import com.Clinivora.model.AuditLog;
import com.Clinivora.util.DateTimeUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class AuditLogController {
    @FXML
    private TextField actionField;
    @FXML
    private TextField fromField;
    @FXML
    private TextField toField;
    @FXML
    private TableView<AuditLog> auditTable;
    @FXML
    private TableColumn<AuditLog, String> createdCol;
    @FXML
    private TableColumn<AuditLog, String> actionCol;
    @FXML
    private TableColumn<AuditLog, String> entityCol;
    @FXML
    private TableColumn<AuditLog, String> entityIdCol;
    @FXML
    private TableColumn<AuditLog, String> userCol;
    @FXML
    private TableColumn<AuditLog, String> detailsCol;

    @FXML
    private void initialize() {
        createdCol.setCellValueFactory(c -> new SimpleStringProperty(
                DateTimeUtil.formatDisplay(c.getValue().getCreatedAt())));
        actionCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAction()));
        entityCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEntityType() == null ? "" : c.getValue().getEntityType()));
        entityIdCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEntityId() == null ? "" : String.valueOf(c.getValue().getEntityId())));
        userCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getUserId() == null ? "" : String.valueOf(c.getValue().getUserId())));
        detailsCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDetails() == null ? "" : c.getValue().getDetails()));
        onFilter();
    }

    @FXML
    private void onFilter() {
        auditTable.setItems(FXCollections.observableArrayList(
                AppContext.getInstance().getAuditService().search(
                        actionField.getText(),
                        fromField.getText(),
                        toField.getText(),
                        300)));
    }

    @FXML
    private void onClear() {
        actionField.clear();
        fromField.clear();
        toField.clear();
        onFilter();
    }
}

package com.Clinivora.ui;

import com.Clinivora.config.AppContext;
import com.Clinivora.model.AppUser;
import com.Clinivora.model.enums.UserRole;
import com.Clinivora.ui.helper.AlertHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class AccountsController {
    @FXML
    private TextField usernameField;
    @FXML
    private TextField displayNameField;
    @FXML
    private ComboBox<UserRole> roleBox;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TableView<AppUser> userTable;
    @FXML
    private TableColumn<AppUser, String> usernameCol;
    @FXML
    private TableColumn<AppUser, String> displayCol;
    @FXML
    private TableColumn<AppUser, String> roleCol;
    @FXML
    private TableColumn<AppUser, String> activeCol;

    @FXML
    private void initialize() {
        roleBox.setItems(FXCollections.observableArrayList(UserRole.values()));
        roleBox.getSelectionModel().select(UserRole.RECEPTION);
        usernameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsername()));
        displayCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDisplayName()));
        roleCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRole().name()));
        activeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActive() ? "Active" : "Inactive"));
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
        onRefresh();
    }

    @FXML
    private void onRefresh() {
        userTable.setItems(FXCollections.observableArrayList(
                AppContext.getInstance().getAccountService().listUsers()));
    }

    @FXML
    private void onCreate() {
        try {
            if (roleBox.getValue() == null) {
                AlertHelper.showError("Create failed", "Select a role");
                return;
            }
            AppUser user = AppContext.getInstance().getAccountService().createUser(
                    usernameField.getText(),
                    displayNameField.getText(),
                    roleBox.getValue(),
                    passwordField.getText());
            AlertHelper.showSuccess("User created", user.getUsername());
            usernameField.clear();
            displayNameField.clear();
            passwordField.clear();
            roleBox.getSelectionModel().select(UserRole.RECEPTION);
            onRefresh();
        } catch (Exception ex) {
            AlertHelper.showError("Create failed", formatError(ex));
        }
    }

    static String formatError(Exception ex) {
        String message = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
        Throwable cause = ex.getCause();
        if (cause != null && cause.getMessage() != null && !cause.getMessage().isBlank()) {
            return message + " — " + cause.getMessage();
        }
        return message;
    }

    @FXML
    private void onActivate() {
        AppUser selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showInfo("Select a user", "Choose a user row first.");
            return;
        }
        try {
            AppContext.getInstance().getAccountService().setActive(selected.getId(), true);
            onRefresh();
        } catch (Exception ex) {
            AlertHelper.showError("Activate failed", formatError(ex));
        }
    }

    @FXML
    private void onDeactivate() {
        AppUser selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showInfo("Select a user", "Choose a user row first.");
            return;
        }
        if (!AlertHelper.confirmDeactivate(selected.getUsername())) {
            return;
        }
        try {
            AppContext.getInstance().getAccountService().setActive(selected.getId(), false);
            onRefresh();
        } catch (Exception ex) {
            AlertHelper.showError("Deactivate failed", formatError(ex));
        }
    }

    @FXML
    private void onResetPassword() {
        AppUser selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showInfo("Select a user", "Choose a user row first.");
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Reset password · " + selected.getUsername());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        PasswordField password = new PasswordField();
        password.setPromptText("New strong password");
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(12));
        grid.addRow(0, new Label("New password"), password);
        Label hint = new Label("Min 8 chars with upper, lower, digit, special (e.g. NurseDemo!2026)");
        hint.getStyleClass().add("muted");
        grid.add(hint, 0, 1, 2, 1);
        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait().ifPresent(result -> {
            if (result != ButtonType.OK) {
                return;
            }
            try {
                AppContext.getInstance().getAccountService().resetPassword(selected.getId(), password.getText());
                AlertHelper.showSuccess("Password reset", selected.getUsername());
            } catch (Exception ex) {
                AlertHelper.showError("Reset failed", formatError(ex));
            }
        });
    }
}

package com.Clinivora.ui.helper;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

import java.util.Optional;

public final class AlertHelper {
    private AlertHelper() {
    }

    public static void showError(String title, String message) {
        show(Alert.AlertType.ERROR, title, message);
    }

    public static void showSuccess(String title, String message) {
        show(Alert.AlertType.INFORMATION, title, message);
    }

    public static void showInfo(String title, String message) {
        show(Alert.AlertType.INFORMATION, title, message);
    }

    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static boolean confirmLogout() {
        return confirm("Logout", "Sign out of Clinivora?");
    }

    public static boolean confirmCancelCase() {
        return confirm("Cancel case", "Cancel this emergency case? This cannot be undone.");
    }

    public static boolean confirmAssign() {
        return confirm("Assign doctor", "Assign the selected doctor to this case?");
    }

    public static boolean confirmStartTreatment() {
        return confirm("Start treatment", "Mark this case as in treatment?");
    }

    public static boolean confirmCloseCase() {
        return confirm("Close case", "Discharge / close this case?");
    }

    public static boolean confirmServeNext() {
        return confirm("Serve next", "Call the next patient from the routine queue?");
    }

    public static boolean confirmSoftDelete(String entityLabel) {
        return confirm("Soft delete", "Deactivate " + entityLabel + "? It can be restored later by an admin.");
    }

    public static boolean confirmDeactivate(String entityLabel) {
        return confirm("Deactivate", "Deactivate " + entityLabel + "?");
    }

    public static boolean confirmRestore(String entityLabel) {
        return confirm("Restore", "Restore " + entityLabel + "?");
    }

    private static void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message == null ? "" : message);
        Window owner = alert.getDialogPane().getScene() == null ? null : alert.getDialogPane().getScene().getWindow();
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.showAndWait();
    }
}

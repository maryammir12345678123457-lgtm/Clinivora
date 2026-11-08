package com.Clinivora.ui;

import com.Clinivora.App;
import com.Clinivora.config.AppContext;
import com.Clinivora.service.NavigationService;
import com.Clinivora.service.NavigationService.View;
import com.Clinivora.ui.helper.AlertHelper;
import com.Clinivora.ui.helper.ViewLoader;
import com.Clinivora.ui.i18n.I18n;
import com.Clinivora.util.DateTimeUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.LinkedHashMap;
import java.util.Map;

public class MainShellController {
    @FXML
    private StackPane contentPane;
    @FXML
    private Label pageTitleLabel;
    @FXML
    private Label pageHintLabel;
    @FXML
    private Label userLabel;
    @FXML
    private Label clockLabel;
    @FXML
    private Label roleChip;
    @FXML
    private Label brandTitleLabel;
    @FXML
    private Label brandSubLabel;
    @FXML
    private Label navSectionLabel;
    @FXML
    private Label navAdminLabel;
    @FXML
    private Button navDashboard;
    @FXML
    private Button navPatients;
    @FXML
    private Button navEmergency;
    @FXML
    private Button navQueue;
    @FXML
    private Button navDoctors;
    @FXML
    private Button navReports;
    @FXML
    private Button navAccounts;
    @FXML
    private Button navAudit;
    @FXML
    private Button navDsa;
    @FXML
    private Button navSettings;
    @FXML
    private Button changePasswordButton;
    @FXML
    private Button logoutButton;

    private final Map<View, Button> navButtons = new LinkedHashMap<>();
    private NavigationService navigation;
    private Timeline clockTimeline;

    @FXML
    private void initialize() {
        AppContext ctx = AppContext.getInstance();
        navigation = ctx.getNavigationService();
        navButtons.put(View.DASHBOARD, navDashboard);
        navButtons.put(View.PATIENTS, navPatients);
        navButtons.put(View.EMERGENCY_ROOM, navEmergency);
        navButtons.put(View.ROUTINE_QUEUE, navQueue);
        navButtons.put(View.DOCTORS, navDoctors);
        navButtons.put(View.REPORTS, navReports);
        navButtons.put(View.ACCOUNTS, navAccounts);
        navButtons.put(View.AUDIT_LOG, navAudit);
        navButtons.put(View.DSA_LAB, navDsa);
        navButtons.put(View.SETTINGS, navSettings);

        applyRoleVisibility();
        localizeChrome();
        updateUserChrome();
        startClock();
        navigation.setOnNavigate(this::renderCurrent);
        I18n.getInstance().addListener(lang -> {
            localizeChrome();
            updateUserChrome();
            renderCurrent();
            if (App.getPrimaryStage() != null) {
                I18n.getInstance().applyOrientation(App.getPrimaryStage().getScene());
                App.getPrimaryStage().setTitle(I18n.getInstance().t("app.title"));
            }
        });
        if (navigation.getCurrent() == View.LOGIN) {
            navigation.resetTo(View.DASHBOARD);
        } else {
            renderCurrent();
        }
    }

    private void startClock() {
        tickClock();
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> tickClock()));
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();
    }

    private void tickClock() {
        if (clockLabel != null) {
            clockLabel.setText(DateTimeUtil.formatDisplay(DateTimeUtil.now()));
        }
    }

    private void updateUserChrome() {
        AppContext ctx = AppContext.getInstance();
        if (ctx.getUserSession().getCurrentUser() != null) {
            userLabel.setText(ctx.getUserSession().getCurrentUser().getDisplayName());
            roleChip.setText(ctx.getUserSession().getRole() == null
                    ? ""
                    : ctx.getUserSession().getRole().name());
            roleChip.setVisible(true);
            roleChip.setManaged(true);
        } else {
            userLabel.setText("");
            roleChip.setVisible(false);
            roleChip.setManaged(false);
        }
    }

    private void localizeChrome() {
        I18n i18n = I18n.getInstance();
        brandTitleLabel.setText(i18n.t("app.title"));
        brandSubLabel.setText(i18n.t("app.subtitle"));
        navSectionLabel.setText(i18n.t("nav.section.workspace"));
        navAdminLabel.setText(i18n.t("nav.section.admin"));
        navDashboard.setText(i18n.t("nav.dashboard"));
        navPatients.setText(i18n.t("nav.patients"));
        navEmergency.setText(i18n.t("nav.emergency"));
        navQueue.setText(i18n.t("nav.queue"));
        navDoctors.setText(i18n.t("nav.doctors"));
        navReports.setText(i18n.t("nav.reports"));
        navAccounts.setText(i18n.t("nav.accounts"));
        navAudit.setText(i18n.t("nav.audit"));
        navDsa.setText(i18n.t("nav.dsa"));
        navSettings.setText(i18n.t("nav.settings"));
        changePasswordButton.setText(i18n.t("nav.changePassword"));
        logoutButton.setText(i18n.t("nav.logout"));
    }

    private void applyRoleVisibility() {
        for (Map.Entry<View, Button> entry : navButtons.entrySet()) {
            entry.getValue().setVisible(navigation.isVisibleForCurrentRole(entry.getKey()));
            entry.getValue().setManaged(entry.getValue().isVisible());
        }
        boolean anyAdmin = navigation.isVisibleForCurrentRole(View.ACCOUNTS)
                || navigation.isVisibleForCurrentRole(View.AUDIT_LOG)
                || navigation.isVisibleForCurrentRole(View.DSA_LAB);
        navAdminLabel.setVisible(anyAdmin);
        navAdminLabel.setManaged(anyAdmin);
    }

    private void renderCurrent() {
        View view = navigation.getCurrent();
        if (view == View.LOGIN) {
            return;
        }
        I18n i18n = I18n.getInstance();
        String titleKey = view.getTitleKey();
        pageTitleLabel.setText(titleKey == null ? i18n.t("app.title") : i18n.t(titleKey));
        pageHintLabel.setText(view == View.DASHBOARD
                ? i18n.t("dashboard.pageHint")
                : i18n.t("shell.pageHint"));
        highlightNav(view);
        Parent page = ViewLoader.load(view.getFxmlPath());
        contentPane.getChildren().setAll(page);
    }

    private void highlightNav(View view) {
        View navKey = switch (view) {
            case PATIENT_PROFILE -> View.PATIENTS;
            case CASE_DETAIL -> View.EMERGENCY_ROOM;
            default -> view;
        };
        for (Map.Entry<View, Button> entry : navButtons.entrySet()) {
            entry.getValue().getStyleClass().remove("active");
            if (entry.getKey() == navKey) {
                entry.getValue().getStyleClass().add("active");
            }
        }
    }

    @FXML
    private void goDashboard() {
        navigation.goTo(View.DASHBOARD);
    }

    @FXML
    private void goPatients() {
        navigation.goTo(View.PATIENTS);
    }

    @FXML
    private void goEmergency() {
        navigation.goTo(View.EMERGENCY_ROOM);
    }

    @FXML
    private void goQueue() {
        navigation.goTo(View.ROUTINE_QUEUE);
    }

    @FXML
    private void goDoctors() {
        navigation.goTo(View.DOCTORS);
    }

    @FXML
    private void goReports() {
        navigation.goTo(View.REPORTS);
    }

    @FXML
    private void goAccounts() {
        navigation.goTo(View.ACCOUNTS);
    }

    @FXML
    private void goAudit() {
        navigation.goTo(View.AUDIT_LOG);
    }

    @FXML
    private void goDsa() {
        navigation.goTo(View.DSA_LAB);
    }

    @FXML
    private void goSettings() {
        navigation.goTo(View.SETTINGS);
    }

    @FXML
    private void onChangePassword() {
        I18n i18n = I18n.getInstance();
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(i18n.t("nav.changePassword"));
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        PasswordField current = new PasswordField();
        PasswordField next = new PasswordField();
        PasswordField confirm = new PasswordField();
        current.setPromptText("Current password");
        next.setPromptText("New password");
        confirm.setPromptText("Confirm new password");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(12));
        grid.addRow(0, new Label("Current"), current);
        grid.addRow(1, new Label("New"), next);
        grid.addRow(2, new Label("Confirm"), confirm);
        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait().ifPresent(result -> {
            if (result != ButtonType.OK) {
                return;
            }
            try {
                AppContext.getInstance().getAccountService()
                        .changePassword(current.getText(), next.getText(), confirm.getText());
                AlertHelper.showSuccess("Password updated", "Your password was changed successfully.");
            } catch (Exception ex) {
                AlertHelper.showError("Change password failed", ex.getMessage());
            }
        });
    }

    @FXML
    private void onLogout() {
        if (!AlertHelper.confirmLogout()) {
            return;
        }
        AppContext.getInstance().getAuthenticationService().logout();
        navigation.resetTo(View.LOGIN);
        App.showLogin();
    }
}

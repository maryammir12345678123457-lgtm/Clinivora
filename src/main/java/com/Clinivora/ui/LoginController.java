package com.Clinivora.ui;

import com.Clinivora.App;
import com.Clinivora.config.AppContext;
import com.Clinivora.service.NavigationService;
import com.Clinivora.ui.i18n.AppLanguage;
import com.Clinivora.ui.i18n.I18n;
import com.Clinivora.ui.theme.AppTheme;
import com.Clinivora.ui.theme.ThemeManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label messageLabel;
    @FXML
    private Label brandLabel;
    @FXML
    private Label subtitleLabel;
    @FXML
    private Label signInLabel;
    @FXML
    private Label demoHintLabel;
    @FXML
    private Label demoPassLabel;
    @FXML
    private Button loginButton;
    @FXML
    private ComboBox<AppTheme> themeBox;
    @FXML
    private ComboBox<AppLanguage> languageBox;

    @FXML
    private void initialize() {
        themeBox.setItems(FXCollections.observableArrayList(AppTheme.values()));
        languageBox.setItems(FXCollections.observableArrayList(AppLanguage.values()));
        themeBox.setConverter(themeConverter());
        languageBox.setConverter(languageConverter());
        themeBox.getSelectionModel().select(ThemeManager.getInstance().getCurrent());
        languageBox.getSelectionModel().select(I18n.getInstance().getLanguage());
        themeBox.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                ThemeManager.getInstance().setTheme(newV);
            }
        });
        languageBox.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                I18n.getInstance().setLanguage(newV);
                if (App.getPrimaryStage() != null) {
                    I18n.getInstance().applyOrientation(App.getPrimaryStage().getScene());
                    App.getPrimaryStage().setTitle(I18n.getInstance().t("app.title"));
                }
                localize();
                themeBox.setConverter(themeConverter());
                languageBox.setConverter(languageConverter());
                themeBox.getSelectionModel().select(ThemeManager.getInstance().getCurrent());
                languageBox.getSelectionModel().select(I18n.getInstance().getLanguage());
            }
        });
        localize();
    }

    private void localize() {
        I18n i18n = I18n.getInstance();
        brandLabel.setText(i18n.t("app.title"));
        subtitleLabel.setText(i18n.t("app.subtitle"));
        signInLabel.setText(i18n.t("login.signIn"));
        usernameField.setPromptText(i18n.t("login.username"));
        passwordField.setPromptText(i18n.t("login.password"));
        loginButton.setText(i18n.t("login.button"));
        demoHintLabel.setText(i18n.t("login.demoHint"));
        demoPassLabel.setText(i18n.t("login.demoPass"));
    }

    private StringConverter<AppTheme> themeConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(AppTheme theme) {
                return theme == null ? "" : I18n.getInstance().t(theme.getI18nKey());
            }

            @Override
            public AppTheme fromString(String string) {
                return null;
            }
        };
    }

    private StringConverter<AppLanguage> languageConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(AppLanguage language) {
                if (language == null) {
                    return "";
                }
                return I18n.getInstance().t(language.getI18nKey()) + " · " + language.getNativeName();
            }

            @Override
            public AppLanguage fromString(String string) {
                return null;
            }
        };
    }

    @FXML
    private void fillAdmin() {
        fill("admin", "AdminDemo!2026");
    }

    @FXML
    private void fillReception() {
        fill("reception", "ReceptionDemo!2026");
    }

    @FXML
    private void fillTriage() {
        fill("triage", "TriageDemo!2026");
    }

    @FXML
    private void fillDoctor() {
        fill("doctor", "DoctorDemo!2026");
    }

    private void fill(String user, String pass) {
        usernameField.setText(user);
        passwordField.setText(pass);
        messageLabel.setText("");
    }

    @FXML
    private void onLogin() {
        try {
            AppContext ctx = AppContext.getInstance();
            ctx.getAuthenticationService().login(usernameField.getText(), passwordField.getText());
            ctx.getNavigationService().resetTo(NavigationService.View.DASHBOARD);
            App.showMainShell();
        } catch (Exception ex) {
            messageLabel.setText(ex.getMessage() == null
                    ? I18n.getInstance().t("login.failed")
                    : ex.getMessage());
        }
    }
}

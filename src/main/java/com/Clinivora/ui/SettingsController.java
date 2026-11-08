package com.Clinivora.ui;

import com.Clinivora.ui.i18n.AppLanguage;
import com.Clinivora.ui.i18n.I18n;
import com.Clinivora.ui.theme.AppTheme;
import com.Clinivora.ui.theme.ThemeManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.util.StringConverter;

public class SettingsController {
    @FXML
    private Label titleLabel;
    @FXML
    private Label hintLabel;
    @FXML
    private Label themeLabel;
    @FXML
    private Label languageLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private ComboBox<AppTheme> themeBox;
    @FXML
    private ComboBox<AppLanguage> languageBox;
    @FXML
    private Button applyButton;

    @FXML
    private void initialize() {
        themeBox.setItems(FXCollections.observableArrayList(AppTheme.values()));
        languageBox.setItems(FXCollections.observableArrayList(AppLanguage.values()));
        themeBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(AppTheme theme) {
                return theme == null ? "" : I18n.getInstance().t(theme.getI18nKey());
            }

            @Override
            public AppTheme fromString(String string) {
                return null;
            }
        });
        languageBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(AppLanguage language) {
                if (language == null) {
                    return "";
                }
                return I18n.getInstance().t(language.getI18nKey()) + " (" + language.getNativeName() + ")";
            }

            @Override
            public AppLanguage fromString(String string) {
                return null;
            }
        });
        themeBox.getSelectionModel().select(ThemeManager.getInstance().getCurrent());
        languageBox.getSelectionModel().select(I18n.getInstance().getLanguage());
        localize();
        I18n.getInstance().addListener(lang -> localize());
    }

    private void localize() {
        I18n i18n = I18n.getInstance();
        titleLabel.setText(i18n.t("settings.title"));
        hintLabel.setText(i18n.t("settings.hint"));
        themeLabel.setText(i18n.t("settings.theme"));
        languageLabel.setText(i18n.t("settings.language"));
        applyButton.setText(i18n.t("settings.apply"));
        themeBox.setItems(FXCollections.observableArrayList(AppTheme.values()));
        languageBox.setItems(FXCollections.observableArrayList(AppLanguage.values()));
        themeBox.getSelectionModel().select(ThemeManager.getInstance().getCurrent());
        languageBox.getSelectionModel().select(I18n.getInstance().getLanguage());
    }

    @FXML
    private void onApply() {
        AppTheme theme = themeBox.getValue();
        AppLanguage language = languageBox.getValue();
        if (theme == null || language == null) {
            statusLabel.getStyleClass().setAll("error-text");
            statusLabel.setText("Select theme and language");
            return;
        }
        ThemeManager.getInstance().setTheme(theme);
        I18n.getInstance().setLanguage(language);
        if (com.Clinivora.App.getPrimaryStage() != null) {
            I18n.getInstance().applyOrientation(com.Clinivora.App.getPrimaryStage().getScene());
        }
        statusLabel.getStyleClass().setAll("success-text");
        statusLabel.setText(I18n.getInstance().t("settings.saved"));
        localize();
    }
}

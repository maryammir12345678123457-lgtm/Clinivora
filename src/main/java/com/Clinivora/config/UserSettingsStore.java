package com.Clinivora.config;

import com.Clinivora.ui.i18n.AppLanguage;
import com.Clinivora.ui.theme.AppTheme;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

/**
 * Persists theme/language preferences under the data directory (no JavaFX dependency).
 */
public class UserSettingsStore {
    public static final String DEFAULT_FILE_NAME = "clinivora-settings.properties";

    private final Path settingsFile;
    private AppTheme theme = AppTheme.LIGHT;
    private AppLanguage language = AppLanguage.ENGLISH;

    public UserSettingsStore() {
        this(AppConfig.getDataDirectory().resolve(DEFAULT_FILE_NAME));
    }

    public UserSettingsStore(Path settingsFile) {
        this.settingsFile = Objects.requireNonNull(settingsFile, "settingsFile");
    }

    public Path getSettingsFile() {
        return settingsFile;
    }

    public AppTheme getTheme() {
        return theme;
    }

    public AppLanguage getLanguage() {
        return language;
    }

    public void load() {
        if (!Files.exists(settingsFile)) {
            return;
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(settingsFile)) {
            props.load(in);
            theme = AppTheme.fromId(props.getProperty("theme", AppTheme.LIGHT.getId()));
            language = AppLanguage.fromCode(props.getProperty("language", AppLanguage.ENGLISH.getCode()));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load settings from " + settingsFile, e);
        } catch (IllegalArgumentException e) {
            theme = AppTheme.LIGHT;
            language = AppLanguage.ENGLISH;
        }
    }

    public void save(AppTheme theme, AppLanguage language) {
        this.theme = Objects.requireNonNull(theme, "theme");
        this.language = Objects.requireNonNull(language, "language");
        Properties props = new Properties();
        props.setProperty("theme", this.theme.getId());
        props.setProperty("language", this.language.getCode());
        try {
            Files.createDirectories(settingsFile.getParent());
            try (OutputStream out = Files.newOutputStream(settingsFile)) {
                props.store(out, "Clinivora user preferences");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save settings to " + settingsFile, e);
        }
    }

    public void setTheme(AppTheme theme) {
        save(theme, this.language);
    }

    public void setLanguage(AppLanguage language) {
        save(this.theme, language);
    }
}

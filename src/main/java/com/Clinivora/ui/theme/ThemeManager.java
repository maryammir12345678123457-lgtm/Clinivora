package com.Clinivora.ui.theme;

import com.Clinivora.App;
import com.Clinivora.config.UserSettingsStore;
import javafx.scene.Scene;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class ThemeManager {
    public static final String BASE_STYLESHEET = "/com/Clinivora/css/app.css";

    private static ThemeManager instance;

    private final UserSettingsStore settingsStore;
    private final List<Consumer<AppTheme>> listeners = new CopyOnWriteArrayList<>();
    private AppTheme current = AppTheme.LIGHT;

    public ThemeManager(UserSettingsStore settingsStore) {
        this.settingsStore = Objects.requireNonNull(settingsStore, "settingsStore");
        this.current = settingsStore.getTheme();
    }

    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ThemeManager not initialized");
        }
        return instance;
    }

    public static synchronized void initialize(UserSettingsStore store) {
        instance = new ThemeManager(store);
    }

    public static synchronized void resetForTests() {
        instance = null;
    }

    public AppTheme getCurrent() {
        return current;
    }

    public void addListener(Consumer<AppTheme> listener) {
        listeners.add(listener);
    }

    public void setTheme(AppTheme theme) {
        Objects.requireNonNull(theme, "theme");
        current = theme;
        settingsStore.setTheme(theme);
        applyToScene(App.getPrimaryStage() == null ? null : App.getPrimaryStage().getScene());
        for (Consumer<AppTheme> listener : listeners) {
            listener.accept(theme);
        }
    }

    public void applyToScene(Scene scene) {
        if (scene == null) {
            return;
        }
        List<String> keep = new ArrayList<>();
        for (String sheet : scene.getStylesheets()) {
            if (!sheet.contains("/com/Clinivora/css/theme-") && !sheet.contains("/com/Clinivora/css/app.css")) {
                keep.add(sheet);
            }
        }
        scene.getStylesheets().setAll(keep);
        String base = App.class.getResource(BASE_STYLESHEET).toExternalForm();
        String theme = App.class.getResource(current.getStylesheetPath()).toExternalForm();
        scene.getStylesheets().add(base);
        scene.getStylesheets().add(theme);
    }

    public List<String> resolveStylesheets(AppTheme theme) {
        return List.of(BASE_STYLESHEET, theme.getStylesheetPath());
    }
}

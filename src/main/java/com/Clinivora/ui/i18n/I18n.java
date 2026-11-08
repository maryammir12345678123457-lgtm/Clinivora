package com.Clinivora.ui.i18n;

import com.Clinivora.config.UserSettingsStore;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class I18n {
    private static final String BUNDLE_BASE = "i18n.messages";

    private static I18n instance;

    private final UserSettingsStore settingsStore;
    private final CopyOnWriteArrayList<Consumer<AppLanguage>> listeners = new CopyOnWriteArrayList<>();
    private AppLanguage language = AppLanguage.ENGLISH;
    private ResourceBundle bundle;

    public I18n(UserSettingsStore settingsStore) {
        this.settingsStore = Objects.requireNonNull(settingsStore, "settingsStore");
        this.language = settingsStore.getLanguage();
        this.bundle = loadBundle(language);
    }

    public static synchronized I18n getInstance() {
        if (instance == null) {
            throw new IllegalStateException("I18n not initialized");
        }
        return instance;
    }

    public static synchronized void initialize(UserSettingsStore store) {
        instance = new I18n(store);
    }

    public static synchronized void resetForTests() {
        instance = null;
    }

    public AppLanguage getLanguage() {
        return language;
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    public void addListener(Consumer<AppLanguage> listener) {
        listeners.add(listener);
    }

    public void setLanguage(AppLanguage language) {
        Objects.requireNonNull(language, "language");
        this.language = language;
        this.bundle = loadBundle(language);
        settingsStore.setLanguage(language);
        for (Consumer<AppLanguage> listener : listeners) {
            listener.accept(language);
        }
    }

    public String t(String key, Object... args) {
        String pattern;
        try {
            pattern = bundle.getString(key);
        } catch (MissingResourceException ex) {
            pattern = key;
        }
        if (args == null || args.length == 0) {
            return pattern;
        }
        return MessageFormat.format(pattern, args);
    }

    public void applyOrientation(Scene scene) {
        if (scene == null) {
            return;
        }
        scene.setNodeOrientation(language.isRightToLeft()
                ? NodeOrientation.RIGHT_TO_LEFT
                : NodeOrientation.LEFT_TO_RIGHT);
        Parent root = scene.getRoot();
        if (root != null) {
            root.setNodeOrientation(language.isRightToLeft()
                    ? NodeOrientation.RIGHT_TO_LEFT
                    : NodeOrientation.LEFT_TO_RIGHT);
        }
    }

    public static ResourceBundle loadBundle(AppLanguage language) {
        Locale locale = language.toLocale();
        return ResourceBundle.getBundle(BUNDLE_BASE, locale);
    }
}

package com.Clinivora.ui.theme;

import java.util.Locale;

public enum AppTheme {
    LIGHT("light", "theme.light"),
    DARK("dark", "theme.dark"),
    PINK("pink", "theme.pink"),
    COLORFUL("colorful", "theme.colorful");

    private final String id;
    private final String i18nKey;

    AppTheme(String id, String i18nKey) {
        this.id = id;
        this.i18nKey = i18nKey;
    }

    public String getId() {
        return id;
    }

    public String getI18nKey() {
        return i18nKey;
    }

    public String getStylesheetPath() {
        return "/com/Clinivora/css/theme-" + id + ".css";
    }

    public static AppTheme fromId(String value) {
        if (value == null || value.isBlank()) {
            return LIGHT;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (AppTheme theme : values()) {
            if (theme.id.equals(normalized) || theme.name().equalsIgnoreCase(normalized)) {
                return theme;
            }
        }
        throw new IllegalArgumentException("Unsupported theme: " + value);
    }
}

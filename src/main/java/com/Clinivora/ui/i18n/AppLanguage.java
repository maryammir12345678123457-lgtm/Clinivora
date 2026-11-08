package com.Clinivora.ui.i18n;

import java.util.Locale;

public enum AppLanguage {
    ENGLISH("en", "English", "language.english", false),
    URDU("ur", "اردو", "language.urdu", true),
    CHINESE("zh", "中文", "language.chinese", false);

    private final String code;
    private final String nativeName;
    private final String i18nKey;
    private final boolean rightToLeft;

    AppLanguage(String code, String nativeName, String i18nKey, boolean rightToLeft) {
        this.code = code;
        this.nativeName = nativeName;
        this.i18nKey = i18nKey;
        this.rightToLeft = rightToLeft;
    }

    public String getCode() {
        return code;
    }

    public String getNativeName() {
        return nativeName;
    }

    public String getI18nKey() {
        return i18nKey;
    }

    public boolean isRightToLeft() {
        return rightToLeft;
    }

    public Locale toLocale() {
        return switch (this) {
            case ENGLISH -> Locale.ENGLISH;
            case URDU -> Locale.forLanguageTag("ur");
            case CHINESE -> Locale.SIMPLIFIED_CHINESE;
        };
    }

    public static AppLanguage fromCode(String value) {
        if (value == null || value.isBlank()) {
            return ENGLISH;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (AppLanguage language : values()) {
            if (language.code.equals(normalized) || language.name().equalsIgnoreCase(normalized)) {
                return language;
            }
        }
        throw new IllegalArgumentException("Unsupported language: " + value);
    }
}

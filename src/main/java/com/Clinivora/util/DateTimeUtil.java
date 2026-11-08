package com.Clinivora.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DateTimeUtil {
    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private DateTimeUtil() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now().withNano(0);
    }

    public static String formatDateTime(LocalDateTime value) {
        return value == null ? null : DATE_TIME.format(value);
    }

    public static String formatDate(LocalDate value) {
        return value == null ? null : DATE.format(value);
    }

    public static String formatDisplay(LocalDateTime value) {
        return value == null ? "" : DISPLAY.format(value);
    }

    public static LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.trim(), DATE_TIME);
        } catch (DateTimeParseException ex) {
            return LocalDateTime.parse(value.trim());
        }
    }

    public static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), DATE);
        } catch (DateTimeParseException ex) {
            return LocalDate.parse(value.trim());
        }
    }
}

package com.Clinivora.util;

public final class DisplayCodeFormatter {
    private DisplayCodeFormatter() {
    }

    public static String patient(long id) {
        return format("PAT", id);
    }

    public static String emergencyCase(long id) {
        return format("EMG", id);
    }

    public static String doctor(long id) {
        return format("DOC", id);
    }

    public static String ticket(long id) {
        return format("TKT", id);
    }

    public static String format(String prefix, long id) {
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("Prefix is required");
        }
        if (id < 0) {
            throw new IllegalArgumentException("Id must be non-negative");
        }
        return prefix.toUpperCase() + "-" + String.format("%06d", id);
    }
}

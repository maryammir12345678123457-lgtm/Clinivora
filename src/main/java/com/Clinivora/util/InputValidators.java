package com.Clinivora.util;

import java.util.regex.Pattern;

public final class InputValidators {
    private static final Pattern USERNAME = Pattern.compile("^[a-zA-Z0-9._-]{3,40}$");
    private static final Pattern PHONE = Pattern.compile("^[+0-9][0-9\\-\\s()]{6,20}$");
    private static final Pattern NAME = Pattern.compile("^[\\p{L} .'-]{1,80}$");

    private InputValidators() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isUsername(String value) {
        return value != null && USERNAME.matcher(value.trim()).matches();
    }

    public static boolean isPhone(String value) {
        return value != null && PHONE.matcher(value.trim()).matches();
    }

    public static boolean isName(String value) {
        return value != null && NAME.matcher(value.trim()).matches();
    }

    public static boolean isStrongPassword(String value) {
        if (value == null || value.length() < 8) {
            return false;
        }
        boolean upper = false;
        boolean lower = false;
        boolean digit = false;
        boolean special = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isUpperCase(c)) {
                upper = true;
            } else if (Character.isLowerCase(c)) {
                lower = true;
            } else if (Character.isDigit(c)) {
                digit = true;
            } else {
                special = true;
            }
        }
        return upper && lower && digit && special;
    }

    public static String requireNonBlank(String value, String fieldName) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }
}

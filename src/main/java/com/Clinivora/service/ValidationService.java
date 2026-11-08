package com.Clinivora.service;

import com.Clinivora.util.InputValidators;

public class ValidationService {

    public String requireName(String value, String field) {
        String trimmed = InputValidators.requireNonBlank(value, field);
        if (!InputValidators.isName(trimmed)) {
            throw new IllegalArgumentException(field + " has invalid characters");
        }
        return trimmed;
    }

    /**
     * Validates and normalizes usernames to lowercase.
     */
    public String requireUsername(String value) {
        if (InputValidators.isBlank(value)) {
            throw new IllegalArgumentException("Username is required");
        }
        String trimmed = value.trim();
        if (trimmed.length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters");
        }
        if (trimmed.length() > 40) {
            throw new IllegalArgumentException("Username must be at most 40 characters");
        }
        if (!InputValidators.isUsername(trimmed)) {
            throw new IllegalArgumentException(
                    "Username may only contain letters, numbers, dots, underscores, or hyphens");
        }
        return trimmed.toLowerCase();
    }

    public String optionalPhone(String value) {
        if (InputValidators.isBlank(value)) {
            return null;
        }
        String trimmed = value.trim();
        if (!InputValidators.isPhone(trimmed)) {
            throw new IllegalArgumentException("Phone number is invalid");
        }
        return trimmed;
    }

    public void requireStrongPassword(String password) {
        if (!InputValidators.isStrongPassword(password)) {
            throw new IllegalArgumentException(
                    "Password must be 8+ chars with upper, lower, digit, and special character");
        }
    }
}

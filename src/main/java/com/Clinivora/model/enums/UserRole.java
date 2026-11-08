package com.Clinivora.model.enums;

public enum UserRole {
    ADMIN,
    RECEPTION,
    TRIAGE,
    DOCTOR;

    public static UserRole fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        return UserRole.valueOf(value.trim().toUpperCase());
    }
}

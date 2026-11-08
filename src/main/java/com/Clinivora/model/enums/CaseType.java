package com.Clinivora.model.enums;

public enum CaseType {
    EMERGENCY,
    ROUTINE;

    public static CaseType fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Case type cannot be null");
        }
        return CaseType.valueOf(value.trim().toUpperCase());
    }
}

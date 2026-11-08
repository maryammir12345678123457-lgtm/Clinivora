package com.Clinivora.model.enums;

public enum AssignmentStatus {
    ACTIVE,
    COMPLETED,
    REASSIGNED;

    public static AssignmentStatus fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Assignment status cannot be null");
        }
        return AssignmentStatus.valueOf(value.trim().toUpperCase());
    }
}

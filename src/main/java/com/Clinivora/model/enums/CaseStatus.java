package com.Clinivora.model.enums;

public enum CaseStatus {
    REGISTERED,
    TRIAGED,
    ASSIGNED,
    IN_TREATMENT,
    DISCHARGED,
    CANCELLED;

    public static CaseStatus fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Case status cannot be null");
        }
        return CaseStatus.valueOf(value.trim().toUpperCase());
    }
}

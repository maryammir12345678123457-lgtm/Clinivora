package com.Clinivora.model.enums;

public enum EventType {
    REGISTERED,
    TRIAGED,
    ASSIGNED,
    TREATMENT_STARTED,
    DISCHARGED,
    QUEUE_JOINED,
    QUEUE_CALLED,
    NOTE,
    CLINICAL_NOTE,
    UPDATED,
    SOFT_DELETED,
    RESTORED,
    OTHER;

    public static EventType fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Event type cannot be null");
        }
        return EventType.valueOf(value.trim().toUpperCase());
    }
}

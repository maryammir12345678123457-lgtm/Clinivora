package com.Clinivora.model.enums;

public enum TicketStatus {
    WAITING,
    CALLED,
    IN_SERVICE,
    DONE,
    CANCELLED;

    public static TicketStatus fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Ticket status cannot be null");
        }
        return TicketStatus.valueOf(value.trim().toUpperCase());
    }
}

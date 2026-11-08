package com.Clinivora.model.enums;

public enum TriagePriority {
    P1(1, "Immediate"),
    P2(2, "Very Urgent"),
    P3(3, "Urgent"),
    P4(4, "Standard"),
    P5(5, "Non-Urgent");

    private final int rank;
    private final String label;

    TriagePriority(int rank, String label) {
        this.rank = rank;
        this.label = label;
    }

    public int getRank() {
        return rank;
    }

    public String getLabel() {
        return label;
    }

    public static TriagePriority fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Priority cannot be null");
        }
        return TriagePriority.valueOf(value.trim().toUpperCase());
    }
}

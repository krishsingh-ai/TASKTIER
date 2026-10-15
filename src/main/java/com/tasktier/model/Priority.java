package com.tasktier.model;

/**
 * Enumeration representing task importance / priority levels.
 * Each level has an associated numerical weight used in priority scoring calculations.
 */
public enum Priority {
    LOW(1, "Low"),
    MEDIUM(2, "Medium"),
    HIGH(3, "High"),
    URGENT(4, "Urgent");

    private final int weight;
    private final String displayName;

    Priority(int weight, String displayName) {
        this.weight = weight;
        this.displayName = displayName;
    }

    /**
     * Retrieves the numerical weight (1 to 4).
     *
     * @return numerical weight
     */
    public int getWeight() {
        return weight;
    }

    /**
     * Retrieves the user-friendly display name.
     *
     * @return display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Parses a string safely into a Priority enum constant.
     *
     * @param value input string
     * @return matching Priority or MEDIUM as default
     */
    public static Priority fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return MEDIUM;
        }
        try {
            return Priority.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return MEDIUM;
        }
    }
}

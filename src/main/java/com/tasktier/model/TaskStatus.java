package com.tasktier.model;

/**
 * Enumeration representing the execution lifecycle status of a task.
 */
public enum TaskStatus {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    OVERDUE("Overdue");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
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
     * Parses a string safely into a TaskStatus enum constant.
     *
     * @param value input string
     * @return matching TaskStatus or PENDING as default
     */
    public static TaskStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return PENDING;
        }
        try {
            return TaskStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return PENDING;
        }
    }
}

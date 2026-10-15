package com.tasktier.model;

/**
 * Enumeration representing the user role and authorization level within TaskTier.
 */
public enum UserRole {
    USER("User"),
    ADMIN("Administrator");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Retrieves the user-friendly role name.
     *
     * @return display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Parses a string safely into a UserRole constant.
     *
     * @param value input string
     * @return matching UserRole or USER as default
     */
    public static UserRole fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return USER;
        }
        try {
            return UserRole.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return USER;
        }
    }
}

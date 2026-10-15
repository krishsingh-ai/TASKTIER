package com.tasktier.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Model class representing an authenticated user of TaskTier.
 * Encapsulates identity, role, credentials, and account activity.
 */
public class User {

    private int id;
    private String username;
    private String displayName;
    private String passwordHash;
    private UserRole role;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    /**
     * Default zero-argument constructor.
     */
    public User() {
        this.role = UserRole.USER;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Parameterized constructor for user registration and initialization.
     *
     * @param username unique username
     * @param displayName full user display name
     * @param passwordHash PBKDF2 hashed password
     * @param role user authorization role
     */
    public User(String username, String displayName, String passwordHash, UserRole role) {
        this();
        this.username = username;
        this.displayName = displayName;
        this.passwordHash = passwordHash;
        this.role = role != null ? role : UserRole.USER;
    }

    /**
     * Retrieves the primary key ID.
     *
     * @return user ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the primary key ID.
     *
     * @param id user ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Retrieves the unique username.
     *
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Retrieves the display name.
     *
     * @return display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name.
     *
     * @param displayName display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Retrieves the password hash.
     *
     * @return password hash string
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Sets the password hash.
     *
     * @param passwordHash hashed password
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Retrieves the authorization role.
     *
     * @return UserRole
     */
    public UserRole getRole() {
        return role;
    }

    /**
     * Sets the authorization role.
     *
     * @param role UserRole
     */
    public void setRole(UserRole role) {
        this.role = role;
    }

    /**
     * Checks if the user account is active.
     *
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the user account active status.
     *
     * @param active status flag
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Retrieves the creation timestamp.
     *
     * @return LocalDateTime creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     *
     * @param createdAt timestamp
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Retrieves the last login timestamp.
     *
     * @return LocalDateTime last login timestamp
     */
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    /**
     * Sets the last login timestamp.
     *
     * @param lastLoginAt timestamp
     */
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return id == user.id && Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", displayName='" + displayName + '\'' +
                ", role=" + role +
                ", active=" + active +
                '}';
    }
}

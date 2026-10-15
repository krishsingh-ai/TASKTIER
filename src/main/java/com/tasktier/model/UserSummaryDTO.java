package com.tasktier.model;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing user account details with aggregated activity metrics
 * for the administrative management panel.
 */
public class UserSummaryDTO {

    private int id;
    private String username;
    private String displayName;
    private String role;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private int tasksCreated;
    private int tasksCompleted;
    private double hoursLogged;

    /**
     * Default no-argument constructor for reflection and serialization frameworks.
     */
    public UserSummaryDTO() {
    }

    /**
     * Parameterized constructor initializing all summary metrics.
     *
     * @param id user primary key identifier
     * @param username user login handle
     * @param displayName full display name
     * @param role security role name
     * @param active account activation status
     * @param createdAt account creation timestamp
     * @param lastLoginAt most recent successful login timestamp
     * @param tasksCreated total tasks created across history
     * @param tasksCompleted total tasks marked complete
     * @param hoursLogged cumulative hours logged via timer
     */
    public UserSummaryDTO(int id, String username, String displayName, String role, boolean active,
                          LocalDateTime createdAt, LocalDateTime lastLoginAt,
                          int tasksCreated, int tasksCompleted, double hoursLogged) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
        this.tasksCreated = tasksCreated;
        this.tasksCompleted = tasksCompleted;
        this.hoursLogged = hoursLogged;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public int getTasksCreated() {
        return tasksCreated;
    }

    public void setTasksCreated(int tasksCreated) {
        this.tasksCreated = tasksCreated;
    }

    public int getTasksCompleted() {
        return tasksCompleted;
    }

    public void setTasksCompleted(int tasksCompleted) {
        this.tasksCompleted = tasksCompleted;
    }

    public double getHoursLogged() {
        return hoursLogged;
    }

    public void setHoursLogged(double hoursLogged) {
        this.hoursLogged = hoursLogged;
    }
}

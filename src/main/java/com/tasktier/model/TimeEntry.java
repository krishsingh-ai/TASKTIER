package com.tasktier.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Model class representing a tracked time duration log associated with a specific task.
 */
public class TimeEntry {

    private int id;
    private int userId;
    private int taskId;
    private String taskTitle;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private String notes;

    /**
     * Default constructor.
     */
    public TimeEntry() {
        this.startTime = LocalDateTime.now();
    }

    /**
     * Parameterized constructor for starting a time tracking session.
     *
     * @param userId user identifier
     * @param taskId task identifier
     * @param notes optional session notes
     */
    public TimeEntry(int userId, int taskId, String notes) {
        this();
        this.userId = userId;
        this.taskId = taskId;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeEntry timeEntry)) return false;
        return id == timeEntry.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TimeEntry{" +
                "id=" + id +
                ", taskId=" + taskId +
                ", durationMinutes=" + durationMinutes +
                ", startTime=" + startTime +
                '}';
    }
}

package com.tasktier.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Model class representing a prioritized task within TaskTier.
 * Implements Comparable to enable natural sorting by calculated priority score.
 */
public class Task implements Comparable<Task> {

    private int id;
    private int userId;
    private String title;
    private String description;
    private Integer categoryId;
    private String categoryName;
    private String categoryColor;
    private Double estimatedHours;
    private LocalDateTime deadline;
    private Priority importance;
    private TaskStatus status;
    private double priorityScore;
    private String tier;
    private LocalDateTime createdAt;

    /**
     * Default constructor.
     */
    public Task() {
        this.importance = Priority.MEDIUM;
        this.status = TaskStatus.PENDING;
        this.priorityScore = 0.0;
        this.tier = "Low";
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Parameterized constructor for task creation.
     *
     * @param userId user ID owning the task
     * @param title task title
     * @param description detailed description
     * @param categoryId category identifier
     * @param estimatedHours estimated completion duration in hours
     * @param deadline deadline timestamp
     * @param importance urgency/importance priority level
     */
    public Task(int userId, String title, String description, Integer categoryId,
                Double estimatedHours, LocalDateTime deadline, Priority importance) {
        this();
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
        this.estimatedHours = estimatedHours;
        this.deadline = deadline;
        this.importance = importance != null ? importance : Priority.MEDIUM;
    }

    /**
     * Returns the Priority Tier classification based on the priority score:
     * Critical (>= 80), High (60-79), Medium (40-59), Low (< 40).
     *
     * @return tier name ("Critical", "High", "Medium", "Low")
     */
    public String getTier() {
        if (tier == null) {
            tier = calculateTier(priorityScore);
        }
        return tier;
    }

    /**
     * Returns CSS badge color class corresponding to the tier.
     *
     * @return badge CSS class name
     */
    public String getTierBadgeClass() {
        if (priorityScore >= 80.0) {
            return "badge-critical";
        } else if (priorityScore >= 60.0) {
            return "badge-high";
        } else if (priorityScore >= 40.0) {
            return "badge-medium";
        } else {
            return "badge-low";
        }
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryColor() {
        return categoryColor;
    }

    public void setCategoryColor(String categoryColor) {
        this.categoryColor = categoryColor;
    }

    public Double getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(Double estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public Priority getImportance() {
        return importance;
    }

    public void setImportance(Priority importance) {
        this.importance = importance;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public double getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(double priorityScore) {
        this.priorityScore = priorityScore;
        this.tier = calculateTier(priorityScore);
    }

    private static String calculateTier(double score) {
        if (score >= 80.0) {
            return "Critical";
        } else if (score >= 60.0) {
            return "High";
        } else if (score >= 40.0) {
            return "Medium";
        } else {
            return "Low";
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Orders tasks descending by priority score (highest score first).
     *
     * @param other comparison target task
     * @return comparison order integer
     */
    @Override
    public int compareTo(Task other) {
        if (other == null) {
            return -1;
        }
        // Descending order of priority score
        return Double.compare(other.priorityScore, this.priorityScore);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task task)) return false;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", importance=" + importance +
                ", status=" + status +
                ", priorityScore=" + priorityScore +
                ", tier='" + getTier() + '\'' +
                '}';
    }
}

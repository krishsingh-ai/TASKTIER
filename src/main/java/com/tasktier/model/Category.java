package com.tasktier.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Model class representing a task classification category.
 * Used to organize tasks by domain, subject, or project.
 */
public class Category {

    private int id;
    private String name;
    private String description;
    private String color;
    private LocalDateTime createdAt;

    /**
     * Default zero-argument constructor.
     */
    public Category() {
        this.color = "#3b82f6";
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Parameterized constructor.
     *
     * @param name category name
     * @param description description
     * @param color hexadecimal color code
     */
    public Category(String name, String description, String color) {
        this();
        this.name = name;
        this.description = description;
        if (color != null && !color.trim().isEmpty()) {
            this.color = color;
        }
    }

    /**
     * Retrieves category ID.
     *
     * @return ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets category ID.
     *
     * @param id ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Retrieves category name.
     *
     * @return category name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets category name.
     *
     * @param name category name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves category description.
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets category description.
     *
     * @param description description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Retrieves color hex code.
     *
     * @return hex color string
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets color hex code.
     *
     * @param color hex color string
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Retrieves creation timestamp.
     *
     * @return creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets creation timestamp.
     *
     * @param createdAt timestamp
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category category)) return false;
        return id == category.id && Objects.equals(name, category.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}

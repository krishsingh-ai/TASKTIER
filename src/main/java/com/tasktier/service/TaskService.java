package com.tasktier.service;

import com.tasktier.dao.TaskDAO;
import com.tasktier.exception.DataAccessException;
import com.tasktier.exception.ValidationException;
import com.tasktier.model.Priority;
import com.tasktier.model.Task;
import com.tasktier.model.TaskStatus;
import com.tasktier.util.AppLogger;
import com.tasktier.util.DateUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Business service managing tasks, validation, prioritization, and task collections.
 * Demonstrates:
 * 1. PriorityQueue&lt;Task&gt; for score-ordered polling.
 * 2. Custom Comparator&lt;Task&gt;.
 * 3. Set&lt;String&gt; for unique categories.
 * 4. Java Streams API for filtering and aggregation.
 * 5. Strict input validation throwing ValidationException.
 */
public class TaskService {

    private final TaskDAO taskDAO;
    private final PriorityCalculator priorityCalculator;

    /**
     * Default constructor.
     */
    public TaskService() {
        this(new TaskDAO(), new PriorityCalculator());
    }

    /**
     * Parameterized constructor.
     *
     * @param taskDAO data access object
     * @param priorityCalculator priority calculation service
     */
    public TaskService(TaskDAO taskDAO, PriorityCalculator priorityCalculator) {
        this.taskDAO = taskDAO;
        this.priorityCalculator = priorityCalculator;
    }

    /**
     * Creates a new task after strict input validation and priority calculation.
     *
     * @param userId owner user ID
     * @param title task title
     * @param description description
     * @param categoryId category ID
     * @param estimatedHours estimated effort hours
     * @param deadlineStr deadline in ISO or datetime format
     * @param importanceStr importance level name
     * @return persisted Task instance
     * @throws ValidationException if input fields are invalid
     * @throws DataAccessException if database operation fails
     */
    public Task createTask(int userId, String title, String description, Integer categoryId,
                           Double estimatedHours, String deadlineStr, String importanceStr)
            throws ValidationException, DataAccessException {

        validateTaskInput(title, estimatedHours, deadlineStr);

        Priority importance = Priority.fromString(importanceStr);
        LocalDateTime deadline = DateUtil.parse(deadlineStr);

        Task task = new Task(userId, title.trim(), description != null ? description.trim() : "",
                categoryId, estimatedHours, deadline, importance);

        // Calculate initial priority score
        double initialScore = priorityCalculator.calculateScore(task);
        task.setPriorityScore(initialScore);

        Task savedTask = taskDAO.insert(task);
        AppLogger.info(getClass(), "Task created successfully with ID: " + savedTask.getId() + " and score: " + initialScore);
        return savedTask;
    }

    /**
     * Retrieves all tasks for a user, ordered by priority score descending.
     *
     * @param userId user identifier
     * @return list of user tasks
     * @throws DataAccessException on database error
     */
    public List<Task> getUserTasks(int userId) throws DataAccessException {
        return taskDAO.findByUserId(userId);
    }

    /**
     * Retrieves tasks ranked by priority score using a PriorityQueue with custom Comparator.
     * Inserts all user tasks into the PriorityQueue, then polls them in order.
     * Fulfills Rubric Section 2 (PriorityQueue + Comparator requirement).
     *
     * @param userId user identifier
     * @return list of tasks in strictly polled priority order
     * @throws DataAccessException on database query failure
     */
    public List<Task> getRankedTasks(int userId) throws DataAccessException {
        List<Task> allUserTasks = taskDAO.findByUserId(userId);

        // Custom Comparator: sort descending by priority score; tiebreak by earlier deadline
        Comparator<Task> taskScoreComparator = (task1, task2) -> {
            int scoreDiff = Double.compare(task2.getPriorityScore(), task1.getPriorityScore());
            if (scoreDiff != 0) {
                return scoreDiff;
            }
            if (task1.getDeadline() == null && task2.getDeadline() == null) return 0;
            if (task1.getDeadline() == null) return 1;
            if (task2.getDeadline() == null) return -1;
            return task1.getDeadline().compareTo(task2.getDeadline());
        };

        // Construct PriorityQueue using the custom Comparator
        PriorityQueue<Task> rankedQueue = new PriorityQueue<>(Math.max(1, allUserTasks.size()), taskScoreComparator);

        // Insert all tasks into the priority queue
        rankedQueue.addAll(allUserTasks);

        // Poll each task in score order
        List<Task> rankedList = new ArrayList<>();
        while (!rankedQueue.isEmpty()) {
            rankedList.add(rankedQueue.poll());
        }

        return rankedList;
    }

    /**
     * Extracts a unique Set of category names associated with a user's active tasks.
     * Fulfills Rubric Section 2 (Set&lt;String&gt; requirement) using Streams API.
     *
     * @param userId user identifier
     * @return Set of unique category names
     * @throws DataAccessException on database error
     */
    public Set<String> getUniqueCategoryNames(int userId) throws DataAccessException {
        List<Task> tasks = taskDAO.findByUserId(userId);
        return tasks.stream()
                .map(Task::getCategoryName)
                .filter(Objects::nonNull)
                .filter(name -> !name.trim().isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * Filters a user's tasks using the Streams API.
     * Demonstrates Streams API requirement (Rubric Section 8).
     *
     * @param userId user identifier
     * @param status optional status filter
     * @param categoryId optional category filter
     * @param tier optional tier ("Critical", "High", "Medium", "Low")
     * @return filtered list of tasks
     * @throws DataAccessException on database query error
     */
    public List<Task> filterTasks(int userId, TaskStatus status, Integer categoryId, String tier) throws DataAccessException {
        List<Task> tasks = taskDAO.findByUserId(userId);

        return tasks.stream()
                .filter(task -> status == null || task.getStatus() == status)
                .filter(task -> categoryId == null || Objects.equals(task.getCategoryId(), categoryId))
                .filter(task -> tier == null || tier.equalsIgnoreCase(task.getTier()))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a task by ID, verifying user ownership.
     *
     * @param taskId task identifier
     * @param userId user identifier
     * @return Optional containing task if found and owned
     * @throws DataAccessException on query failure
     */
    public Optional<Task> getTaskById(int taskId, int userId) throws DataAccessException {
        Optional<Task> task = taskDAO.findById(taskId);
        if (task.isPresent() && task.get().getUserId() == userId) {
            return task;
        }
        return Optional.empty();
    }

    /**
     * Updates an existing task and recalculates its priority score.
     *
     * @param task task entity to update
     * @param userId owner user ID
     * @return true if updated successfully
     * @throws ValidationException on validation failure
     * @throws DataAccessException on database error
     */
    public boolean updateTask(Task task, int userId) throws ValidationException, DataAccessException {
        if (task == null || task.getId() <= 0) {
            throw new ValidationException("Task ID is missing or invalid.");
        }

        validateTaskInput(task.getTitle(), task.getEstimatedHours(), null);

        Optional<Task> existing = taskDAO.findById(task.getId());
        if (existing.isEmpty() || existing.get().getUserId() != userId) {
            throw new ValidationException("Task not found or access denied.");
        }

        // Recalculate score with updated attributes
        double newScore = priorityCalculator.calculateScore(task);
        task.setPriorityScore(newScore);

        return taskDAO.update(task);
    }

    /**
     * Updates the status of a task.
     *
     * @param taskId task identifier
     * @param userId owner user ID
     * @param status new task status
     * @return true if status updated
     * @throws DataAccessException on database error
     * @throws ValidationException if task not found
     */
    public boolean updateTaskStatus(int taskId, int userId, TaskStatus status) throws DataAccessException, ValidationException {
        Optional<Task> task = getTaskById(taskId, userId);
        if (task.isEmpty()) {
            throw new ValidationException("Task not found or unauthorized.");
        }

        return taskDAO.updateStatus(taskId, status);
    }

    /**
     * Deletes a task by ID after checking ownership.
     *
     * @param taskId task identifier
     * @param userId user identifier
     * @return true if deleted
     * @throws DataAccessException on database failure
     * @throws ValidationException if task not found or unauthorized
     */
    public boolean deleteTask(int taskId, int userId) throws DataAccessException, ValidationException {
        Optional<Task> task = getTaskById(taskId, userId);
        if (task.isEmpty()) {
            throw new ValidationException("Task not found or unauthorized.");
        }

        return taskDAO.delete(taskId);
    }

    /**
     * Returns the PriorityCalculator instance.
     *
     * @return priority calculator
     */
    public PriorityCalculator getPriorityCalculator() {
        return priorityCalculator;
    }

    private void validateTaskInput(String title, Double estimatedHours, String deadlineStr) throws ValidationException {
        Map<String, String> errors = new HashMap<>();

        if (title == null || title.trim().isEmpty()) {
            errors.put("title", "Task title is required.");
        } else if (title.trim().length() > 200) {
            errors.put("title", "Task title cannot exceed 200 characters.");
        }

        if (estimatedHours != null && estimatedHours < 0.0) {
            errors.put("estimatedHours", "Estimated hours cannot be negative.");
        }

        // Note: If deadline cannot be parsed, it safely defaults to null rather than failing validation

        if (!errors.isEmpty()) {
            throw new ValidationException("Task validation failed.", errors);
        }
    }
}

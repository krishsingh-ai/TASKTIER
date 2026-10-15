package com.tasktier.service;

import com.tasktier.dao.TaskDAO;
import com.tasktier.dao.TimeEntryDAO;
import com.tasktier.exception.DataAccessException;
import com.tasktier.exception.ValidationException;
import com.tasktier.model.Task;
import com.tasktier.model.TaskStatus;
import com.tasktier.model.TimeEntry;
import com.tasktier.util.AppLogger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service managing task time tracking, active timer sessions, and duration aggregations.
 * Leverages LocalDateTime and Duration for precise time arithmetic.
 */
public class TimeTrackerService {

    private final TimeEntryDAO timeEntryDAO;
    private final TaskDAO taskDAO;

    /**
     * Default constructor.
     */
    public TimeTrackerService() {
        this(new TimeEntryDAO(), new TaskDAO());
    }

    /**
     * Parameterized constructor.
     *
     * @param timeEntryDAO data access object for time entries
     * @param taskDAO data access object for tasks
     */
    public TimeTrackerService(TimeEntryDAO timeEntryDAO, TaskDAO taskDAO) {
        this.timeEntryDAO = timeEntryDAO;
        this.taskDAO = taskDAO;
    }

    /**
     * Starts a new timer session for a task.
     * Automatically transitions task status to IN_PROGRESS if currently PENDING.
     *
     * @param userId user identifier
     * @param taskId task identifier
     * @param notes optional session notes
     * @return newly created TimeEntry
     * @throws ValidationException if task not found or a timer is already active
     * @throws DataAccessException on database operation failure
     */
    public TimeEntry startTimer(int userId, int taskId, String notes) throws ValidationException, DataAccessException {
        Optional<Task> taskOptional = taskDAO.findById(taskId);
        if (taskOptional.isEmpty() || taskOptional.get().getUserId() != userId) {
            throw new ValidationException("Task not found or access denied.");
        }

        // Check for already running timer for this task
        Optional<TimeEntry> activeOptional = timeEntryDAO.findActiveEntry(userId, taskId);
        if (activeOptional.isPresent()) {
            throw new ValidationException("A timer is already running for this task.");
        }

        TimeEntry entry = new TimeEntry(userId, taskId, notes);
        entry.setStartTime(LocalDateTime.now());

        TimeEntry created = timeEntryDAO.insert(entry);

        // Transition task status to IN_PROGRESS if PENDING
        Task task = taskOptional.get();
        if (task.getStatus() == TaskStatus.PENDING) {
            taskDAO.updateStatus(taskId, TaskStatus.IN_PROGRESS);
        }

        AppLogger.info(getClass(), "Timer started for task ID: " + taskId + " by user ID: " + userId);
        return created;
    }

    /**
     * Stops the currently running timer session for a task and computes elapsed duration.
     *
     * @param userId user identifier
     * @param taskId task identifier
     * @return completed TimeEntry with duration populated
     * @throws ValidationException if no active timer is found
     * @throws DataAccessException on database operation failure
     */
    public TimeEntry stopTimer(int userId, int taskId) throws ValidationException, DataAccessException {
        Optional<TimeEntry> activeOptional = timeEntryDAO.findActiveEntry(userId, taskId);
        if (activeOptional.isEmpty()) {
            throw new ValidationException("No active timer found to stop for this task.");
        }

        TimeEntry active = activeOptional.get();
        LocalDateTime endTime = LocalDateTime.now();
        active.setEndTime(endTime);

        // Compute duration using java.time.Duration
        Duration elapsed = Duration.between(active.getStartTime(), endTime);
        long minutes = Math.max(1, elapsed.toMinutes()); // at least 1 minute recorded
        active.setDurationMinutes((int) minutes);

        timeEntryDAO.update(active);
        AppLogger.info(getClass(), "Timer stopped for task ID: " + taskId + ". Elapsed: " + minutes + " min.");
        return active;
    }

    /**
     * Checks if a timer is currently active for the given task.
     *
     * @param userId user identifier
     * @param taskId task identifier
     * @return Optional containing active TimeEntry if running
     * @throws DataAccessException on database query failure
     */
    public Optional<TimeEntry> getActiveTimer(int userId, int taskId) throws DataAccessException {
        return timeEntryDAO.findActiveEntry(userId, taskId);
    }

    /**
     * Retrieves all recorded time entries for a task.
     *
     * @param taskId task identifier
     * @return list of time entries
     * @throws DataAccessException on database query failure
     */
    public List<TimeEntry> getTaskTimeLogs(int taskId) throws DataAccessException {
        return timeEntryDAO.findByTaskId(taskId);
    }

    /**
     * Retrieves all recorded time entries for a user across all tasks.
     *
     * @param userId user identifier
     * @return list of time entries
     * @throws DataAccessException on database query failure
     */
    public List<TimeEntry> getUserTimeLogs(int userId) throws DataAccessException {
        return timeEntryDAO.findByUserId(userId);
    }

    /**
     * Calculates total time logged in minutes for a user.
     *
     * @param userId user identifier
     * @return total minutes
     * @throws DataAccessException on database error
     */
    public long getTotalUserMinutes(int userId) throws DataAccessException {
        return timeEntryDAO.getTotalMinutesForUser(userId);
    }
}

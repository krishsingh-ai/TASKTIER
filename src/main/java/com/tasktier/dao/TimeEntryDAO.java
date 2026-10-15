package com.tasktier.dao;

import com.tasktier.exception.DataAccessException;
import com.tasktier.model.TimeEntry;
import com.tasktier.util.AppLogger;
import com.tasktier.util.DBConnection;
import com.tasktier.util.DateUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for TimeEntry records.
 * Manages tracking logs, durations, and task-specific or user-wide time aggregations.
 */
public class TimeEntryDAO extends BaseDAO<TimeEntry, Integer> {

    @Override
    protected String getTableName() {
        return "time_entries";
    }

    @Override
    protected TimeEntry mapRow(ResultSet resultSet) throws SQLException {
        TimeEntry entry = new TimeEntry();
        entry.setId(resultSet.getInt("id"));
        entry.setUserId(resultSet.getInt("user_id"));
        entry.setTaskId(resultSet.getInt("task_id"));
        entry.setStartTime(DateUtil.parse(resultSet.getString("start_time")));
        entry.setEndTime(DateUtil.parse(resultSet.getString("end_time")));

        int duration = resultSet.getInt("duration_minutes");
        if (!resultSet.wasNull()) {
            entry.setDurationMinutes(duration);
        }

        entry.setNotes(resultSet.getString("notes"));

        try {
            entry.setTaskTitle(resultSet.getString("task_title"));
        } catch (SQLException ignored) {
            // Not present in default queries
        }

        return entry;
    }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO time_entries (user_id, task_id, start_time, end_time, duration_minutes, notes) " +
               "VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, TimeEntry entry) throws SQLException {
        statement.setInt(1, entry.getUserId());
        statement.setInt(2, entry.getTaskId());
        statement.setString(3, DateUtil.formatForDb(entry.getStartTime()));
        statement.setString(4, DateUtil.formatForDb(entry.getEndTime()));

        if (entry.getDurationMinutes() != null) {
            statement.setInt(5, entry.getDurationMinutes());
        } else {
            statement.setNull(5, java.sql.Types.INTEGER);
        }

        statement.setString(6, entry.getNotes());
    }

    @Override
    protected void setGeneratedId(TimeEntry entry, int generatedId) {
        entry.setId(generatedId);
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE time_entries SET start_time = ?, end_time = ?, duration_minutes = ?, notes = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, TimeEntry entry) throws SQLException {
        statement.setString(1, DateUtil.formatForDb(entry.getStartTime()));
        statement.setString(2, DateUtil.formatForDb(entry.getEndTime()));

        if (entry.getDurationMinutes() != null) {
            statement.setInt(3, entry.getDurationMinutes());
        } else {
            statement.setNull(3, java.sql.Types.INTEGER);
        }

        statement.setString(4, entry.getNotes());
        statement.setInt(5, entry.getId());
    }

    /**
     * Retrieves all time logs associated with a particular task.
     *
     * @param taskId task identifier
     * @return list of time entries ordered by start time descending
     * @throws DataAccessException on query execution error
     */
    public List<TimeEntry> findByTaskId(int taskId) throws DataAccessException {
        String sql = "SELECT te.*, t.title AS task_title FROM time_entries te " +
                     "JOIN tasks t ON te.task_id = t.id WHERE te.task_id = ? ORDER BY te.start_time DESC";

        List<TimeEntry> entries = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, taskId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    entries.add(mapRow(resultSet));
                }
            }
            return entries;
        } catch (SQLException exception) {
            AppLogger.error(getClass(), "Failed to find time entries for task ID: " + taskId, exception);
            throw new DataAccessException("Error querying time entries for task", exception);
        }
    }

    /**
     * Retrieves all time logs recorded by a specific user.
     *
     * @param userId user identifier
     * @return list of time entries
     * @throws DataAccessException on query execution error
     */
    public List<TimeEntry> findByUserId(int userId) throws DataAccessException {
        String sql = "SELECT te.*, t.title AS task_title FROM time_entries te " +
                     "JOIN tasks t ON te.task_id = t.id WHERE te.user_id = ? ORDER BY te.start_time DESC";

        List<TimeEntry> entries = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    entries.add(mapRow(resultSet));
                }
            }
            return entries;
        } catch (SQLException exception) {
            AppLogger.error(getClass(), "Failed to find time entries for user ID: " + userId, exception);
            throw new DataAccessException("Error querying user time entries", exception);
        }
    }

    /**
     * Computes total logged minutes for all tasks of a user.
     *
     * @param userId user identifier
     * @return total minutes logged
     * @throws DataAccessException on database query failure
     */
    public long getTotalMinutesForUser(int userId) throws DataAccessException {
        String sql = "SELECT COALESCE(SUM(duration_minutes), 0) FROM time_entries WHERE user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
                return 0L;
            }
        } catch (SQLException exception) {
            AppLogger.error(getClass(), "Failed to calculate total minutes for user ID: " + userId, exception);
            throw new DataAccessException("Error calculating total user time", exception);
        }
    }

    /**
     * Finds any currently running (unstopped) time entry for a specific task and user.
     *
     * @param userId user identifier
     * @param taskId task identifier
     * @return Optional containing the active TimeEntry if found
     * @throws DataAccessException on database query failure
     */
    public Optional<TimeEntry> findActiveEntry(int userId, int taskId) throws DataAccessException {
        String sql = "SELECT * FROM time_entries WHERE user_id = ? AND task_id = ? AND end_time IS NULL LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setInt(2, taskId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            AppLogger.error(getClass(), "Failed to find active timer for task ID: " + taskId, exception);
            throw new DataAccessException("Error checking active time entry", exception);
        }
    }
}

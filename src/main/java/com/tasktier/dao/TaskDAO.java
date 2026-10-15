package com.tasktier.dao;

import com.tasktier.exception.DataAccessException;
import com.tasktier.model.Priority;
import com.tasktier.model.Task;
import com.tasktier.model.TaskStatus;
import com.tasktier.util.AppLogger;
import com.tasktier.util.DBConnection;
import com.tasktier.util.DateUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Task entities.
 * Handles task persistence, priority score updates, status changes, and user-scoped queries.
 */
public class TaskDAO extends BaseDAO<Task, Integer> {

    @Override
    protected String getTableName() {
        return "tasks";
    }

    @Override
    protected Task mapRow(ResultSet resultSet) throws SQLException {
        Task task = new Task();
        task.setId(resultSet.getInt("id"));
        task.setUserId(resultSet.getInt("user_id"));
        task.setTitle(resultSet.getString("title"));
        task.setDescription(resultSet.getString("description"));

        int categoryId = resultSet.getInt("category_id");
        if (!resultSet.wasNull()) {
            task.setCategoryId(categoryId);
        }

        double estHours = resultSet.getDouble("estimated_hours");
        if (!resultSet.wasNull()) {
            task.setEstimatedHours(estHours);
        }

        task.setDeadline(DateUtil.parse(resultSet.getString("deadline")));
        task.setImportance(Priority.fromString(resultSet.getString("importance")));
        task.setStatus(TaskStatus.fromString(resultSet.getString("status")));
        task.setPriorityScore(resultSet.getDouble("priority_score"));
        task.setCreatedAt(DateUtil.parse(resultSet.getString("created_at")));

        // Optional joined columns
        try {
            task.setCategoryName(resultSet.getString("category_name"));
            task.setCategoryColor(resultSet.getString("category_color"));
        } catch (SQLException ignored) {
            // Joined columns might not be present in basic SELECT * queries
        }

        return task;
    }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO tasks (user_id, title, description, category_id, estimated_hours, deadline, " +
               "importance, status, priority_score, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, Task task) throws SQLException {
        statement.setInt(1, task.getUserId());
        statement.setString(2, task.getTitle());
        statement.setString(3, task.getDescription());

        if (task.getCategoryId() != null) {
            statement.setInt(4, task.getCategoryId());
        } else {
            statement.setNull(4, java.sql.Types.INTEGER);
        }

        if (task.getEstimatedHours() != null) {
            statement.setDouble(5, task.getEstimatedHours());
        } else {
            statement.setNull(5, java.sql.Types.REAL);
        }

        statement.setString(6, DateUtil.formatForDb(task.getDeadline()));
        statement.setString(7, task.getImportance() != null ? task.getImportance().name() : Priority.MEDIUM.name());
        statement.setString(8, task.getStatus() != null ? task.getStatus().name() : TaskStatus.PENDING.name());
        statement.setDouble(9, task.getPriorityScore());
        statement.setString(10, DateUtil.formatForDb(task.getCreatedAt() != null ? task.getCreatedAt() : LocalDateTime.now()));
    }

    @Override
    protected void setGeneratedId(Task task, int generatedId) {
        task.setId(generatedId);
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE tasks SET title = ?, description = ?, category_id = ?, estimated_hours = ?, " +
               "deadline = ?, importance = ?, status = ?, priority_score = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, Task task) throws SQLException {
        statement.setString(1, task.getTitle());
        statement.setString(2, task.getDescription());

        if (task.getCategoryId() != null) {
            statement.setInt(3, task.getCategoryId());
        } else {
            statement.setNull(3, java.sql.Types.INTEGER);
        }

        if (task.getEstimatedHours() != null) {
            statement.setDouble(4, task.getEstimatedHours());
        } else {
            statement.setNull(4, java.sql.Types.REAL);
        }

        statement.setString(5, DateUtil.formatForDb(task.getDeadline()));
        statement.setString(6, task.getImportance() != null ? task.getImportance().name() : Priority.MEDIUM.name());
        statement.setString(7, task.getStatus() != null ? task.getStatus().name() : TaskStatus.PENDING.name());
        statement.setDouble(8, task.getPriorityScore());
        statement.setInt(9, task.getId());
    }

    /**
     * Finds a task by ID including category join details.
     *
     * @param id task primary key identifier
     * @return Optional containing Task if present, empty Optional otherwise
     * @throws DataAccessException on query execution failure
     */
    @Override
    public Optional<Task> findById(Integer id) throws DataAccessException {
        String sql = "SELECT t.*, c.name AS category_name, c.color AS category_color " +
                     "FROM tasks t LEFT JOIN categories c ON t.category_id = c.id WHERE t.id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            AppLogger.error(getClass(), "Failed to find task by ID: " + id, exception);
            throw new DataAccessException("Error finding task by ID", exception);
        }
    }

    /**
     * Retrieves all tasks belonging to a specific user, enriched with category metadata.
     *
     * @param userId user identifier
     * @return list of tasks ordered by priority score descending
     * @throws DataAccessException on query execution failure
     */
    public List<Task> findByUserId(int userId) throws DataAccessException {
        String sql = "SELECT t.*, c.name AS category_name, c.color AS category_color " +
                     "FROM tasks t LEFT JOIN categories c ON t.category_id = c.id " +
                     "WHERE t.user_id = ? ORDER BY t.priority_score DESC";

        List<Task> tasks = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tasks.add(mapRow(resultSet));
                }
            }
            return tasks;
        } catch (SQLException exception) {
            AppLogger.error(getClass(), "Failed to find tasks for user ID: " + userId, exception);
            throw new DataAccessException("Error finding tasks by user ID", exception);
        }
    }

    /**
     * Updates the status of a specific task.
     *
     * @param taskId task identifier
     * @param status new task status
     * @return true if updated successfully
     * @throws DataAccessException on database access failure
     */
    public boolean updateStatus(int taskId, TaskStatus status) throws DataAccessException {
        String sql = "UPDATE tasks SET status = ? WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.name());
            statement.setInt(2, taskId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            AppLogger.error(getClass(), "Failed to update status for task ID: " + taskId, exception);
            throw new DataAccessException("Error updating task status", exception);
        }
    }

    /**
     * Updates the calculated priority score for a task.
     *
     * @param taskId task identifier
     * @param priorityScore newly calculated score
     * @return true if updated successfully
     * @throws DataAccessException on database access failure
     */
    public boolean updatePriorityScore(int taskId, double priorityScore) throws DataAccessException {
        String sql = "UPDATE tasks SET priority_score = ? WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(1, priorityScore);
            statement.setInt(2, taskId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            AppLogger.error(getClass(), "Failed to update priority score for task ID: " + taskId, exception);
            throw new DataAccessException("Error updating task priority score", exception);
        }
    }

    /**
     * Retrieves all pending tasks that have passed their deadline.
     *
     * @param currentTimestamp current date-time string
     * @return list of overdue tasks
     * @throws DataAccessException on database query failure
     */
    public List<Task> findOverdueTasks(String currentTimestamp) throws DataAccessException {
        String sql = "SELECT * FROM tasks WHERE status != 'COMPLETED' AND status != 'OVERDUE' " +
                     "AND deadline IS NOT NULL AND deadline < ?";

        List<Task> tasks = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, currentTimestamp);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tasks.add(mapRow(resultSet));
                }
            }
            return tasks;
        } catch (SQLException exception) {
            AppLogger.error(getClass(), "Failed to query overdue tasks", exception);
            throw new DataAccessException("Error finding overdue tasks", exception);
        }
    }
}

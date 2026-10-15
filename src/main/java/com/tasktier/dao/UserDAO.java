package com.tasktier.dao;

import com.tasktier.exception.DataAccessException;
import com.tasktier.model.User;
import com.tasktier.model.UserRole;
import com.tasktier.model.UserSummaryDTO;
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
 * Data Access Object for User entities.
 * Handles database persistence, credential lookup, and account status management.
 */
public class UserDAO extends BaseDAO<User, Integer> {

    @Override
    protected String getTableName() {
        return "users";
    }

    @Override
    protected User mapRow(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setUsername(resultSet.getString("username"));
        user.setDisplayName(resultSet.getString("display_name"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setRole(UserRole.fromString(resultSet.getString("role")));
        user.setActive(resultSet.getInt("is_active") == 1);
        user.setCreatedAt(DateUtil.parse(resultSet.getString("created_at")));
        user.setLastLoginAt(DateUtil.parse(resultSet.getString("last_login_at")));
        return user;
    }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO users (username, display_name, password_hash, role, is_active, created_at) " +
               "VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, User user) throws SQLException {
        statement.setString(1, user.getUsername());
        statement.setString(2, user.getDisplayName());
        statement.setString(3, user.getPasswordHash());
        statement.setString(4, user.getRole() != null ? user.getRole().name() : UserRole.USER.name());
        statement.setInt(5, user.isActive() ? 1 : 0);
        statement.setString(6, DateUtil.formatForDb(user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now()));
    }

    @Override
    protected void setGeneratedId(User user, int generatedId) {
        user.setId(generatedId);
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE users SET display_name = ?, password_hash = ?, role = ?, is_active = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, User user) throws SQLException {
        statement.setString(1, user.getDisplayName());
        statement.setString(2, user.getPasswordHash());
        statement.setString(3, user.getRole() != null ? user.getRole().name() : UserRole.USER.name());
        statement.setInt(4, user.isActive() ? 1 : 0);
        statement.setInt(5, user.getId());
    }

    /**
     * Finds a user by their case-insensitive username.
     *
     * @param username username to search for
     * @return Optional containing User if found
     * @throws DataAccessException on query execution failure
     */
    public Optional<User> findByUsername(String username) throws DataAccessException {
        String sql = "SELECT * FROM users WHERE username = ? COLLATE NOCASE";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            AppLogger.error(getClass(), "Failed to look up user by username: " + username, exception);
            throw new DataAccessException("Error searching for user by username", exception);
        }
    }

    /**
     * Updates the last login timestamp for the given user ID to the current time.
     *
     * @param userId user identifier
     * @return true if updated successfully
     * @throws DataAccessException on query execution failure
     */
    public boolean updateLastLogin(int userId) throws DataAccessException {
        String sql = "UPDATE users SET last_login_at = ? WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, DateUtil.formatForDb(LocalDateTime.now()));
            statement.setInt(2, userId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            AppLogger.error(getClass(), "Failed to update last login for user ID: " + userId, exception);
            throw new DataAccessException("Error updating last login timestamp", exception);
        }
    }

    /**
     * Deactivates a user account.
     *
     * @param userId user identifier
     * @return true if successfully deactivated
     * @throws DataAccessException on query execution failure
     */
    public boolean deactivate(int userId) throws DataAccessException {
        String sql = "UPDATE users SET is_active = 0 WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            AppLogger.error(getClass(), "Failed to deactivate user ID: " + userId, exception);
            throw new DataAccessException("Error deactivating user", exception);
        }
    }

    /**
     * Reactivates a user account.
     *
     * @param userId user identifier
     * @return true if successfully reactivated
     * @throws DataAccessException on query execution failure
     */
    public boolean reactivate(int userId) throws DataAccessException {
        String sql = "UPDATE users SET is_active = 1 WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            AppLogger.error(getClass(), "Failed to reactivate user ID: " + userId, exception);
            throw new DataAccessException("Error reactivating user", exception);
        }
    }

    /**
     * Retrieves aggregated user metrics for the admin management console.
     * Calculates Tasks Created, Tasks Completed, and Hours Logged using scalar subqueries.
     *
     * @return list of UserSummaryDTOs
     * @throws DataAccessException on query execution failure
     */
    public List<UserSummaryDTO> getUserSummaries() throws DataAccessException {
        String sql = "SELECT u.id, u.username, u.display_name, u.role, u.is_active, u.created_at, u.last_login_at, " +
                     "(SELECT COUNT(*) FROM tasks t WHERE t.user_id = u.id) AS tasks_created, " +
                     "(SELECT COUNT(*) FROM tasks t WHERE t.user_id = u.id AND t.status = 'COMPLETED') AS tasks_completed, " +
                     "(SELECT COALESCE(SUM(duration_minutes), 0) / 60.0 FROM time_entries te WHERE te.user_id = u.id) AS hours_logged " +
                     "FROM users u ORDER BY u.id ASC";

        List<UserSummaryDTO> summaries = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                UserSummaryDTO dto = new UserSummaryDTO();
                dto.setId(resultSet.getInt("id"));
                dto.setUsername(resultSet.getString("username"));
                dto.setDisplayName(resultSet.getString("display_name"));
                dto.setRole(resultSet.getString("role"));
                dto.setActive(resultSet.getInt("is_active") == 1);
                dto.setCreatedAt(DateUtil.parse(resultSet.getString("created_at")));
                dto.setLastLoginAt(DateUtil.parse(resultSet.getString("last_login_at")));
                dto.setTasksCreated(resultSet.getInt("tasks_created"));
                dto.setTasksCompleted(resultSet.getInt("tasks_completed"));
                dto.setHoursLogged(resultSet.getDouble("hours_logged"));
                summaries.add(dto);
            }
            return summaries;
        } catch (SQLException exception) {
            AppLogger.error(getClass(), "Failed to query user summaries", exception);
            throw new DataAccessException("Error retrieving user summaries", exception);
        }
    }
}

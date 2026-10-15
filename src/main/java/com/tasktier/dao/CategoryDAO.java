package com.tasktier.dao;

import com.tasktier.exception.DataAccessException;
import com.tasktier.model.Category;
import com.tasktier.util.AppLogger;
import com.tasktier.util.DBConnection;
import com.tasktier.util.DateUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Data Access Object for Category entities.
 */
public class CategoryDAO extends BaseDAO<Category, Integer> {

    @Override
    protected String getTableName() {
        return "categories";
    }

    @Override
    protected Category mapRow(ResultSet resultSet) throws SQLException {
        Category category = new Category();
        category.setId(resultSet.getInt("id"));
        category.setName(resultSet.getString("name"));
        category.setDescription(resultSet.getString("description"));
        category.setColor(resultSet.getString("color"));
        category.setCreatedAt(DateUtil.parse(resultSet.getString("created_at")));
        return category;
    }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO categories (name, description, color, created_at) VALUES (?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, Category category) throws SQLException {
        statement.setString(1, category.getName());
        statement.setString(2, category.getDescription());
        statement.setString(3, category.getColor());
        statement.setString(4, DateUtil.formatForDb(category.getCreatedAt() != null ? category.getCreatedAt() : LocalDateTime.now()));
    }

    @Override
    protected void setGeneratedId(Category category, int generatedId) {
        category.setId(generatedId);
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE categories SET name = ?, description = ?, color = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, Category category) throws SQLException {
        statement.setString(1, category.getName());
        statement.setString(2, category.getDescription());
        statement.setString(3, category.getColor());
        statement.setInt(4, category.getId());
    }

    /**
     * Finds a category by its name.
     *
     * @param name category name
     * @return Optional containing Category if found
     * @throws DataAccessException on database access error
     */
    public Optional<Category> findByName(String name) throws DataAccessException {
        String sql = "SELECT * FROM categories WHERE name = ? COLLATE NOCASE";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            AppLogger.error(getClass(), "Failed to look up category by name: " + name, exception);
            throw new DataAccessException("Error searching for category by name", exception);
        }
    }
}

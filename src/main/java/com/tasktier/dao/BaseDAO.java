package com.tasktier.dao;

import com.tasktier.exception.DataAccessException;
import com.tasktier.util.AppLogger;
import com.tasktier.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Abstract generic Data Access Object (DAO) providing core CRUD capabilities.
 * Implements the Template Method pattern and ensures robust resource management via try-with-resources.
 *
 * @param <T> entity type
 * @param <ID> identifier type (typically Integer)
 */
public abstract class BaseDAO<T, ID> implements Persistable<T, ID> {

    /**
     * Returns the underlying database table name for this entity.
     *
     * @return table name
     */
    protected abstract String getTableName();

    /**
     * Maps the current row of a ResultSet to an entity instance of type T.
     *
     * @param resultSet active ResultSet cursor
     * @return mapped entity
     * @throws SQLException on database column read error
     */
    protected abstract T mapRow(ResultSet resultSet) throws SQLException;

    /**
     * SQL INSERT statement with '?' parameter placeholders.
     *
     * @return insert SQL string
     */
    protected abstract String getInsertSql();

    /**
     * Binds entity fields to the PreparedStatement parameter placeholders for insertion.
     *
     * @param statement PreparedStatement to populate
     * @param entity source entity
     * @throws SQLException on parameter binding failure
     */
    protected abstract void setInsertParameters(PreparedStatement statement, T entity) throws SQLException;

    /**
     * Populates the database-generated primary key onto the entity.
     *
     * @param entity target entity
     * @param generatedId auto-incremented ID
     */
    protected abstract void setGeneratedId(T entity, int generatedId);

    /**
     * SQL UPDATE statement with '?' parameter placeholders.
     *
     * @return update SQL string
     */
    protected abstract String getUpdateSql();

    /**
     * Binds entity fields to the PreparedStatement parameter placeholders for an update.
     *
     * @param statement PreparedStatement to populate
     * @param entity source entity
     * @throws SQLException on parameter binding failure
     */
    protected abstract void setUpdateParameters(PreparedStatement statement, T entity) throws SQLException;

    /**
     * Retrieves an entity by its primary key identifier.
     *
     * @param id primary key identifier
     * @return Optional containing entity if found, empty Optional otherwise
     * @throws DataAccessException on underlying database or mapping failure
     */
    @Override
    public Optional<T> findById(ID id) throws DataAccessException {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT * FROM ")
                    .append(getTableName())
                    .append(" WHERE id = ?");

        String sql = queryBuilder.toString();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setObject(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            AppLogger.error(getClass(), "Error executing findById for table: " + getTableName(), exception);
            throw new DataAccessException("Failed to find entity by ID in " + getTableName(), exception);
        }
    }

    /**
     * Retrieves all entities stored in the table.
     *
     * @return list of entities
     * @throws DataAccessException on query execution failure
     */
    @Override
    public List<T> findAll() throws DataAccessException {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT * FROM ").append(getTableName());

        String sql = queryBuilder.toString();
        List<T> results = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                results.add(mapRow(resultSet));
            }
            return results;
        } catch (SQLException exception) {
            AppLogger.error(getClass(), "Error executing findAll for table: " + getTableName(), exception);
            throw new DataAccessException("Failed to retrieve all entities from " + getTableName(), exception);
        }
    }

    /**
     * Inserts a new entity and assigns the database-generated primary key.
     *
     * @param entity the entity to persist
     * @return the persisted entity with generated primary key
     * @throws DataAccessException on insert execution failure
     */
    @Override
    public T insert(T entity) throws DataAccessException {
        String sql = getInsertSql();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setInsertParameters(statement, entity);
            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new DataAccessException("Creating entity in " + getTableName() + " failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    setGeneratedId(entity, generatedKeys.getInt(1));
                }
            }

            return entity;
        } catch (SQLException exception) {
            AppLogger.error(getClass(), "Error inserting entity into " + getTableName(), exception);
            throw new DataAccessException("Failed to insert entity into " + getTableName(), exception);
        }
    }

    /**
     * Updates an existing entity's persisted state.
     *
     * @param entity the entity with updated values
     * @return true if row was updated, false otherwise
     * @throws DataAccessException on update execution failure
     */
    @Override
    public boolean update(T entity) throws DataAccessException {
        String sql = getUpdateSql();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            setUpdateParameters(statement, entity);
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException exception) {
            AppLogger.error(getClass(), "Error updating entity in " + getTableName(), exception);
            throw new DataAccessException("Failed to update entity in " + getTableName(), exception);
        }
    }

    /**
     * Deletes an entity by primary key identifier.
     *
     * @param id primary key identifier
     * @return true if row was deleted, false otherwise
     * @throws DataAccessException on delete execution failure
     */
    @Override
    public boolean delete(ID id) throws DataAccessException {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("DELETE FROM ")
                    .append(getTableName())
                    .append(" WHERE id = ?");

        String sql = queryBuilder.toString();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setObject(1, id);
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException exception) {
            AppLogger.error(getClass(), "Error deleting entity from " + getTableName(), exception);
            throw new DataAccessException("Failed to delete entity from " + getTableName(), exception);
        }
    }
}

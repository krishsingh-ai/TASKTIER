package com.tasktier.dao;

import com.tasktier.exception.DataAccessException;
import java.util.List;
import java.util.Optional;

/**
 * Generic persistence interface defining standard CRUD operations.
 * Demonstrates Java Generics (<T, ID>) and abstraction in the data layer.
 *
 * @param <T> entity type
 * @param <ID> identifier type (e.g. Integer)
 */
public interface Persistable<T, ID> {

    /**
     * Finds an entity by its unique identifier.
     *
     * @param id identifier
     * @return Optional containing entity if found, empty otherwise
     * @throws DataAccessException if a database error occurs
     */
    Optional<T> findById(ID id) throws DataAccessException;

    /**
     * Retrieves all entities of type T from the database.
     *
     * @return list of entities
     * @throws DataAccessException if a database error occurs
     */
    List<T> findAll() throws DataAccessException;

    /**
     * Inserts a new entity into the database.
     *
     * @param entity the entity to persist
     * @return the persisted entity with generated keys populated
     * @throws DataAccessException if a database error occurs
     */
    T insert(T entity) throws DataAccessException;

    /**
     * Updates an existing entity in the database.
     *
     * @param entity the entity with updated values
     * @return true if updated successfully, false if not found
     * @throws DataAccessException if a database error occurs
     */
    boolean update(T entity) throws DataAccessException;

    /**
     * Deletes an entity by its identifier.
     *
     * @param id identifier of entity to delete
     * @return true if deleted, false if not found
     * @throws DataAccessException if a database error occurs
     */
    boolean delete(ID id) throws DataAccessException;
}

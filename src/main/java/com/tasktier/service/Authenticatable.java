package com.tasktier.service;

import com.tasktier.exception.AuthException;
import com.tasktier.model.User;

import java.util.Optional;

/**
 * Interface defining authentication and authorization operations.
 * Part of the OOP design demonstrating interface segregation.
 */
public interface Authenticatable {

    /**
     * Authenticates a user against provided credentials.
     *
     * @param username user login handle
     * @param password raw password
     * @return Optional containing the authenticated User upon success
     * @throws AuthException if authentication fails or account is deactivated
     */
    Optional<User> authenticate(String username, String password) throws AuthException;

    /**
     * Verifies if the user is authorized to access a given protected resource.
     *
     * @param user target user
     * @param resource identifier or path of resource
     * @return true if authorized, false otherwise
     */
    boolean isAuthorized(User user, String resource);
}

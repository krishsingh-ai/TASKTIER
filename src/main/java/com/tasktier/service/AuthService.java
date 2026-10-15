package com.tasktier.service;

import com.tasktier.dao.UserDAO;
import com.tasktier.exception.AuthException;
import com.tasktier.exception.DataAccessException;
import com.tasktier.exception.ValidationException;
import com.tasktier.model.User;
import com.tasktier.model.UserRole;
import com.tasktier.model.UserSummaryDTO;
import com.tasktier.util.AppLogger;
import com.tasktier.util.PasswordHasher;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service managing user authentication, registration, access control, and user caching.
 * Implements Authenticatable and maintains an in-memory user cache (Map&lt;Integer, User&gt;).
 */
public class AuthService implements Authenticatable {

    private final UserDAO userDAO;

    /**
     * In-memory cache for fast user lookup by primary key ID.
     * Demonstrates Collections &amp; Generics requirement (Map&lt;Integer, User&gt;).
     */
    private final Map<Integer, User> userCache = new ConcurrentHashMap<>();

    /**
     * Default constructor initializing UserDAO.
     */
    public AuthService() {
        this(new UserDAO());
    }

    /**
     * Parameterized constructor for dependency injection or testing.
     *
     * @param userDAO user data access object
     */
    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Authenticates a user using BCrypt password hashing.
     *
     * @param username user login handle
     * @param password raw plain-text password
     * @return Optional containing the authenticated User
     * @throws AuthException if credentials are invalid or user is deactivated
     */
    @Override
    public Optional<User> authenticate(String username, String password) throws AuthException {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new AuthException("Username and password are required.");
        }

        try {
            Optional<User> userOptional = userDAO.findByUsername(username.trim());
            if (userOptional.isEmpty()) {
                AppLogger.warn(getClass(), "Authentication failed: username not found -> " + username);
                throw new AuthException("Invalid username or password.");
            }

            User user = userOptional.get();

            if (!user.isActive()) {
                AppLogger.warn(getClass(), "Authentication blocked: account deactivated -> " + username);
                throw new AuthException("Your account has been deactivated.");
            }

            boolean passwordValid = PasswordHasher.verify(password, user.getPasswordHash());
            if (!passwordValid) {
                AppLogger.warn(getClass(), "Authentication failed: invalid password for -> " + username);
                throw new AuthException("Invalid username or password.");
            }

            // Update last login timestamp
            userDAO.updateLastLogin(user.getId());
            user.setLastLoginAt(LocalDateTime.now());

            // Cache user
            userCache.put(user.getId(), user);

            AppLogger.info(getClass(), "User authenticated successfully: " + username);
            return Optional.of(user);

        } catch (DataAccessException exception) {
            AppLogger.error(getClass(), "Database error during authentication", exception);
            throw new AuthException("Authentication service temporarily unavailable.");
        }
    }

    /**
     * Registers a new user after comprehensive field validation.
     *
     * @param username username handle
     * @param displayName full name
     * @param password raw password
     * @return newly created User
     * @throws ValidationException on validation failure
     * @throws DataAccessException on database persistence failure
     */
    public User register(String username, String displayName, String password) throws ValidationException, DataAccessException {
        Map<String, String> errors = new HashMap<>();

        if (username == null || username.trim().length() < 3) {
            errors.put("username", "Username must be at least 3 characters long.");
        } else if (!username.trim().matches("^[a-zA-Z0-9_.-]+$")) {
            errors.put("username", "Username may only contain letters, numbers, underscores, dots, or hyphens.");
        }

        if (displayName == null || displayName.trim().isEmpty()) {
            errors.put("displayName", "Display name cannot be empty.");
        }

        if (password == null || password.length() < 6) {
            errors.put("password", "Password must be at least 6 characters long.");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("User registration validation failed.", errors);
        }

        // Check uniqueness
        Optional<User> existing = userDAO.findByUsername(username.trim());
        if (existing.isPresent()) {
            errors.put("username", "Username is already taken.");
            throw new ValidationException("Username already in use.", errors);
        }

        String hashedPassword = PasswordHasher.hash(password);
        User newUser = new User(username.trim(), displayName.trim(), hashedPassword, UserRole.USER);

        User savedUser = userDAO.insert(newUser);
        userCache.put(savedUser.getId(), savedUser);

        AppLogger.info(getClass(), "New user registered: " + savedUser.getUsername());
        return savedUser;
    }

    /**
     * Determines if a user possesses authorization privileges for a target resource route.
     *
     * @param user user model to verify
     * @param resource requested URI resource path
     * @return true if authorized, false otherwise
     */
    @Override
    public boolean isAuthorized(User user, String resource) {
        if (user == null || !user.isActive()) {
            return false;
        }
        if (resource != null && resource.startsWith("/admin")) {
            return user.getRole() == UserRole.ADMIN;
        }
        return true;
    }

    /**
     * Looks up a user by ID using cache-first retrieval.
     *
     * @param userId user identifier
     * @return Optional containing User
     * @throws DataAccessException on database error
     */
    public Optional<User> getUserById(int userId) throws DataAccessException {
        if (userCache.containsKey(userId)) {
            return Optional.of(userCache.get(userId));
        }
        Optional<User> user = userDAO.findById(userId);
        user.ifPresent(u -> userCache.put(u.getId(), u));
        return user;
    }

    /**
     * Lists all registered users (for admin panel).
     *
     * @return list of all users
     * @throws DataAccessException on database query failure
     */
    public List<User> getAllUsers() throws DataAccessException {
        return userDAO.findAll();
    }

    /**
     * Lists all registered users with summary metrics for admin management panel.
     *
     * @return list of user summaries
     * @throws DataAccessException on database query failure
     */
    public List<UserSummaryDTO> getUserSummaries() throws DataAccessException {
        return userDAO.getUserSummaries();
    }

    /**
     * Deactivates a user account and removes them from the cache.
     *
     * @param userId user identifier
     * @return true if deactivated
     * @throws DataAccessException on error
     */
    public boolean deactivateUser(int userId) throws DataAccessException {
        boolean success = userDAO.deactivate(userId);
        if (success) {
            userCache.remove(userId);
            AppLogger.info(getClass(), "User deactivated: ID " + userId);
        }
        return success;
    }

    /**
     * Reactivates a user account.
     *
     * @param userId user identifier
     * @return true if reactivated
     * @throws DataAccessException on error
     */
    public boolean reactivateUser(int userId) throws DataAccessException {
        boolean success = userDAO.reactivate(userId);
        if (success) {
            userCache.remove(userId);
            AppLogger.info(getClass(), "User reactivated: ID " + userId);
        }
        return success;
    }

    /**
     * Returns an unmodifiable snapshot of the in-memory user cache.
     *
     * @return user cache map
     */
    public Map<Integer, User> getUserCache() {
        return Map.copyOf(userCache);
    }
}

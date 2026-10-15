package com.tasktier.exception;

/**
 * Exception thrown when a database or data access operation fails.
 * Wraps low-level SQLExceptions to isolate the persistence layer and maps to HTTP 500.
 */
public class DataAccessException extends TaskTierException {

    /**
     * Constructs a DataAccessException with an error message.
     *
     * @param message error description
     */
    public DataAccessException(String message) {
        super(message, 500);
    }

    /**
     * Constructs a DataAccessException with an error message and root cause.
     *
     * @param message error description
     * @param cause underlying SQLException or other root cause
     */
    public DataAccessException(String message, Throwable cause) {
        super(message, cause, 500);
    }
}

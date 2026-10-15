package com.tasktier.exception;

/**
 * Base custom checked exception for the TaskTier application.
 * All domain-specific exceptions extend this class and provide an HTTP status code mapping.
 */
public class TaskTierException extends Exception {

    private final int httpStatusCode;

    /**
     * Constructs a new TaskTierException with a message and default HTTP 500 status.
     *
     * @param message human-readable error description
     */
    public TaskTierException(String message) {
        this(message, 500);
    }

    /**
     * Constructs a new TaskTierException with a message and specific HTTP status code.
     *
     * @param message human-readable error description
     * @param httpStatusCode mapped HTTP status code
     */
    public TaskTierException(String message, int httpStatusCode) {
        super(message);
        this.httpStatusCode = httpStatusCode;
    }

    /**
     * Constructs a new TaskTierException with a message, cause, and specific HTTP status code.
     *
     * @param message human-readable error description
     * @param cause underlying cause
     * @param httpStatusCode mapped HTTP status code
     */
    public TaskTierException(String message, Throwable cause, int httpStatusCode) {
        super(message, cause);
        this.httpStatusCode = httpStatusCode;
    }

    /**
     * Retrieves the mapped HTTP status code.
     *
     * @return HTTP status code
     */
    public int getHttpStatusCode() {
        return httpStatusCode;
    }
}

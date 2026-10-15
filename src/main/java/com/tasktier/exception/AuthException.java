package com.tasktier.exception;

/**
 * Exception thrown when authentication or authorization operations fail.
 * Maps to HTTP 401 Unauthorized or HTTP 403 Forbidden.
 */
public class AuthException extends TaskTierException {

    /**
     * Constructs an AuthException with a message and default HTTP 401 status.
     *
     * @param message failure description
     */
    public AuthException(String message) {
        super(message, 401);
    }

    /**
     * Constructs an AuthException with a message and custom HTTP status (e.g. 403).
     *
     * @param message failure description
     * @param httpStatusCode mapped HTTP status code
     */
    public AuthException(String message, int httpStatusCode) {
        super(message, httpStatusCode);
    }
}

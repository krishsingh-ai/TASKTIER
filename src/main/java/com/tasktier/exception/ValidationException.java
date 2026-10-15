package com.tasktier.exception;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown when business validation fails for user input.
 * Encapsulates field-specific validation error messages and maps to HTTP 400 Bad Request.
 */
public class ValidationException extends TaskTierException {

    private final Map<String, String> fieldErrors;

    /**
     * Constructs a ValidationException with a general error message.
     *
     * @param message validation failure message
     */
    public ValidationException(String message) {
        super(message, 400);
        this.fieldErrors = new HashMap<>();
    }

    /**
     * Constructs a ValidationException with specific field errors.
     *
     * @param message general error summary
     * @param fieldErrors map of field names to error messages
     */
    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message, 400);
        this.fieldErrors = fieldErrors != null ? new HashMap<>(fieldErrors) : new HashMap<>();
    }

    /**
     * Constructs a ValidationException for a single invalid field.
     *
     * @param field input field name
     * @param error description of the validation failure
     */
    public static ValidationException singleField(String field, String error) {
        Map<String, String> errors = new HashMap<>();
        errors.put(field, error);
        return new ValidationException(error, errors);
    }

    /**
     * Retrieves an unmodifiable view of all field errors.
     *
     * @return map of field name to error message
     */
    public Map<String, String> getFieldErrors() {
        return Collections.unmodifiableMap(fieldErrors);
    }
}

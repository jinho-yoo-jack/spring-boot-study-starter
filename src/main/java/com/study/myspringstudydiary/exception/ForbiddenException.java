package com.study.myspringstudydiary.exception;

/**
 * Exception thrown when a user attempts to access a resource they don't own
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String resourceName, Long id) {
        super(String.format("You do not have permission to access %s with ID: %d", resourceName, id));
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}

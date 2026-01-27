package com.study.myspringstudydiary.exception;

/**
 * Exception thrown when attempting to create a resource that already exists
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resourceName, String field) {
        super(String.format("%s already exists with %s", resourceName, field));
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
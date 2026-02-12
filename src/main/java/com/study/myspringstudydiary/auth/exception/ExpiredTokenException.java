package com.study.myspringstudydiary.auth.exception;

/**
 * Expired Token Exception
 * Thrown when JWT token has expired
 */
public class ExpiredTokenException extends AuthException {

    public ExpiredTokenException(String message) {
        super(message);
    }

    public ExpiredTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}

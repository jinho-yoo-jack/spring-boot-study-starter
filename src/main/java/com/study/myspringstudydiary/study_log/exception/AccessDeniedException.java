package com.study.myspringstudydiary.study_log.exception;

/**
 * Access Denied Exception
 * 리소스에 대한 접근 권한이 없을 때 발생하는 예외
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
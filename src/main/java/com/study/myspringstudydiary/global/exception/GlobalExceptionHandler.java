package com.study.myspringstudydiary.global.exception;

import com.study.myspringstudydiary.auth.exception.AuthException;
import com.study.myspringstudydiary.auth.exception.ExpiredTokenException;
import com.study.myspringstudydiary.auth.exception.InvalidTokenException;
import com.study.myspringstudydiary.global.common.ApiResponse;
import com.study.myspringstudydiary.study_log.exception.ResourceNotFoundException;
import com.study.myspringstudydiary.study_log.exception.DuplicateResourceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for all controllers
 *
 * @RestControllerAdvice: Handles exceptions across the whole application
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle ResourceNotFoundException
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
            ResourceNotFoundException e) {

        log.warn("리소스를 찾을 수 없음: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("RESOURCE_NOT_FOUND", e.getMessage()));
    }

    /**
     * Handle DuplicateResourceException
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateResource(
            DuplicateResourceException e) {

        log.warn("중복 리소스 오류: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("DUPLICATE_RESOURCE", e.getMessage()));
    }

    /**
     * Handle validation errors from @Valid in @RequestBody
     * RequestBody 검증 실패 시 발생
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException e) {

        Map<String, String> errors = new HashMap<>();

        e.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );

        String message = errors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));

        log.warn("입력값 검증 실패: {}", message);
        log.debug("검증 오류 상세: {}", errors);

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.<Map<String, String>>builder()
                        .success(false)
                        .errorCode("VALIDATION_ERROR")
                        .errorMessage(message)
                        .data(errors)
                        .build());
    }

    /**
     * Handle validation errors from @PathVariable and @RequestParam
     * PathVariable, RequestParam 검증 실패 시 발생
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException e) {

        String message = e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));

        log.warn("파라미터 검증 실패: {}", message);

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", message));
    }

    /**
     * Handle IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
            IllegalArgumentException e) {

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("INVALID_ARGUMENT", e.getMessage()));
    }

    /**
     * Handle BadCredentialsException (invalid username or password)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
            BadCredentialsException e) {

        log.warn("인증 실패 - 잘못된 인증 정보: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("BAD_CREDENTIALS", e.getMessage()));
    }

    /**
     * Handle AuthenticationException (general authentication errors)
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(
            AuthenticationException e) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("AUTHENTICATION_FAILED", e.getMessage()));
    }

    /**
     * Handle AuthException (custom authentication exceptions)
     */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthException(
            AuthException e) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("AUTH_ERROR", e.getMessage()));
    }

    /**
     * Handle InvalidTokenException
     */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidToken(
            InvalidTokenException e) {

        log.warn("유효하지 않은 토큰: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("INVALID_TOKEN", e.getMessage()));
    }

    /**
     * Handle ExpiredTokenException
     */
    @ExceptionHandler(ExpiredTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleExpiredToken(
            ExpiredTokenException e) {

        log.warn("만료된 토큰: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("TOKEN_EXPIRED", e.getMessage()));
    }

    /**
     * Handle general exceptions (unexpected errors)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception e) {

        // Log the error for debugging
        log.error("예상치 못한 에러 발생: {}", e.getMessage(), e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR",
                    "An internal server error occurred"));
    }
}
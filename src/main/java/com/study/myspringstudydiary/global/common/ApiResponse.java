package com.study.myspringstudydiary.global.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Standard API Response wrapper with Lombok
 *
 * Provides consistent response format across all API endpoints
 *
 * @param <T> The type of data in the response
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    /**
     * Indicates whether the request was successful
     */
    private boolean success;

    /**
     * Response data (null if error)
     */
    private T data;

    /**
     * Error code (null if successful)
     */
    private String errorCode;

    /**
     * Error message (null if successful)
     */
    private String errorMessage;

    /**
     * Create success response with data
     *
     * @param data Response data
     * @param <T> Type of data
     * @return ApiResponse with success=true
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    /**
     * Create error response
     *
     * @param errorCode Error code
     * @param errorMessage Error message
     * @param <T> Type of data (will be null)
     * @return ApiResponse with success=false
     */
    public static <T> ApiResponse<T> error(String errorCode, String errorMessage) {
        return ApiResponse.<T>builder()
                .success(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }
}

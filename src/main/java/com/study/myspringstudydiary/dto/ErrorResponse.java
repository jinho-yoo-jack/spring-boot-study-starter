package com.study.myspringstudydiary.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ErrorResponse {

    @JsonProperty("error_code")
    private String errorCode;

    private String message;

    private List<FieldError> errors;

    private LocalDateTime timestamp;

    private String path;

    public static ErrorResponse of(String errorCode, String message, String path) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ErrorResponse of(String errorCode, String message, List<FieldError> errors, String path) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .errors(errors)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Getter
    @Builder
    public static class FieldError {
        private String field;
        private Object value;
        private String reason;

        public static FieldError of(String field, Object value, String reason) {
            return FieldError.builder()
                    .field(field)
                    .value(value)
                    .reason(reason)
                    .build();
        }
    }
}
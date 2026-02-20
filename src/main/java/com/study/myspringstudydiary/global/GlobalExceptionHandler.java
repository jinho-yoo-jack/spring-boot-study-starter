package com.study.myspringstudydiary.global;

import com.study.myspringstudydiary.controller.StudyLogController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> exceptionHandler(Exception e){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.name().toUpperCase(),
                        e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> illegalExceptionHandler(Exception e){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.name().toUpperCase(),
                        e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));
    }
}

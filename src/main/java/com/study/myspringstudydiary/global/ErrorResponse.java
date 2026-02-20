package com.study.myspringstudydiary.global;

import lombok.Getter;

@Getter
public class ErrorResponse {
    private String status;
    private String message;
    private String errorCode;

    private ErrorResponse(){}

    public static ErrorResponse error(String status, String message, String errorCode){
        ErrorResponse response = new ErrorResponse();
        response.errorCode = errorCode;
        response.message = message;
        response.status = status;

        return response;

    }
}

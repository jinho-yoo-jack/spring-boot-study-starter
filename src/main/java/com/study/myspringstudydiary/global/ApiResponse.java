package com.study.myspringstudydiary.global;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiResponse<T> {
    private T data;
    private String status;
    private String message;

    private ApiResponse(){};

    public static <T> ApiResponse<T> success(T data){
        ApiResponse<T> response = new ApiResponse<>();
        response.data = data;
        response.message = "SUCCESS";
        response.status = HttpStatus.OK.toString();

        return response;
    }

}

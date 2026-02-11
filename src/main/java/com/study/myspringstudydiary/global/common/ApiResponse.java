package com.study.myspringstudydiary.global.common;

public class ApiResponse<T> {

    private boolean success;
    private T data;
    private ErrorInfo error;

    // 성공 응답 생성
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        response.error = null;
        return response;
    }

    // 실패 응답 생성
    public static <T> ApiResponse<T> error(String code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.data = null;
        response.error = new ErrorInfo(code, message);
        return response;
    }

    // Getter 메서드들
    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public ErrorInfo getError() { return error; }

    // 에러 정보 내부 클래스
    public static class ErrorInfo {
        private String code;
        private String message;

        public ErrorInfo(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() { return code; }
        public String getMessage() { return message; }
    }
}
package com.study.myspringstudydiary.global.common;


import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Builder
@Slf4j
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ErrorInfo error;

    // 성공 응답 생성
    public static <T> ApiResponse<T> success(T data) {
        boolean isSuccess = true;
        // 인자 값이 5개 이상 넘어갈 때,
        // 인자 값의 순서를 잘못 넣거나, 잘못된 값을 넣었을 때, 휴먼 에러가 발생할 수도 있고,
        // 가독성이 떨어지기 때문에
        // Builder 패턴을 사용하면 휴먼 에러를 방지할 수 있고, 가독성이 올라간다.
        return ApiResponse.<T>builder()
                .success(isSuccess)
                .data(data)
                .build();
    }

    // 실패 응답 생성
    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorInfo(code, message));
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
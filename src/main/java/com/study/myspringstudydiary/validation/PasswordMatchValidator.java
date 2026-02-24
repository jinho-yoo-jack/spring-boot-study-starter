package com.study.myspringstudydiary.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, Object> {

    private String passwordField;
    private String confirmPasswordField;
    private String message;

    @Override
    public void initialize(PasswordMatch constraintAnnotation) {
        // "password" (기본값) 또는 사용자가 지정한 필드명
        this.passwordField = constraintAnnotation.passwordField();

        // "confirmPassword" (기본값) 또는 사용자가 지정한 필드명
        this.confirmPasswordField = constraintAnnotation.confirmPasswordField();

        // "비밀번호가 일치하지 않습니다" 또는 사용자가 지정한 메시지
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            // 1단계: 리플렉션으로 두 필드의 값을 꺼냄
            // SignUpRequest에서 "password" 필드의 값: "myPassword123"
            Object password = getFieldValue(value, passwordField);
            // SignUpRequest에서 "confirmPassword" 필드의 값: "myPassword123"
            Object confirmPassword = getFieldValue(value, confirmPasswordField);

            // 2단계: 두 값 비교
            // password가 null이 아니고, 두 값이 같으면 true
            boolean isValid = password != null && password.equals(confirmPassword);

            // 3단계: 실패 시 에러 메시지를 특정 필드에 바인딩
            if (!isValid) {
                // 기본 메시지 비활성화
                context.disableDefaultConstraintViolation();
                // confirmPassword 필드에 에러 메시지 추가
                context.buildConstraintViolationWithTemplate(message)
                        .addPropertyNode(confirmPasswordField)
                        .addConstraintViolation();
            }

            return isValid;

        } catch (Exception e) {
            return false;
        }
    }

    private Object getFieldValue(Object object, String fieldName) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }
}

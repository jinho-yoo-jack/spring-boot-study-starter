package com.study.myspringstudydiary.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Signup Request DTO
 * Contains user information for registration
 */
@Schema(description = "회원가입 요청 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupRequest {

    @Schema(description = "사용자명", example = "testuser", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Schema(description = "이메일 주소", example = "test@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Schema(description = "비밀번호", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @Schema(description = "전체 이름", example = "홍길동", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;
}

package com.study.myspringstudydiary.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Signup Response DTO
 * Contains information about the newly created user
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupResponse {

    private Long userId;
    private String username;
    private String email;
    private String message;

    /**
     * Create signup response with success message
     */
    public static SignupResponse of(Long userId, String username, String email) {
        return SignupResponse.builder()
                .userId(userId)
                .username(username)
                .email(email)
                .message("User registered successfully")
                .build();
    }
}

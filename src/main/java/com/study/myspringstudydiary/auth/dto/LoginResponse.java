package com.study.myspringstudydiary.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Login Response DTO
 * Contains access token, refresh token, and user information
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn; // in seconds
    private String username;
    private String email;

    /**
     * Create login response with default token type
     */
    public static LoginResponse of(
            String accessToken,
            String refreshToken,
            Long expiresIn,
            String username,
            String email) {

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .username(username)
                .email(email)
                .build();
    }
}

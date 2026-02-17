package com.study.myspringstudydiary.auth.controller;

import com.study.myspringstudydiary.auth.dto.*;
import com.study.myspringstudydiary.auth.service.AuthService;
import com.study.myspringstudydiary.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication REST Controller
 * Provides endpoints for user authentication and registration
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerApi {

    private final AuthService authService;

    /**
     * User login
     * POST /api/auth/login
     */
    @Override
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for username: {}", request.getUsername());

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * User signup/registration
     * POST /api/auth/signup
     */
    @Override
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        log.info("Signup request for username: {}, email: {}", request.getUsername(), request.getEmail());

        SignupResponse response = authService.signup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * Refresh access token
     * POST /api/auth/refresh
     */
    @Override
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request");

        TokenResponse response = authService.refresh(request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * User logout (optional - invalidates refresh token)
     * POST /api/auth/logout
     */
    @Override
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Logout request");

        authService.logout(request.getRefreshToken());

        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }
}

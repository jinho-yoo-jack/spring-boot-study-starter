package com.study.myspringstudydiary.controller;

import com.study.myspringstudydiary.dto.request.LoginRequest;
import com.study.myspringstudydiary.dto.request.SignupRequest;
import com.study.myspringstudydiary.dto.response.AuthResponse;
import com.study.myspringstudydiary.global.common.ApiResponse;
import com.study.myspringstudydiary.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@Valid @RequestBody SignupRequest request) {
        log.info("Signup request received for username: {}", request.getUsername());

        AuthResponse authResponse = userService.signup(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(authResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for username: {}", request.getUsername());

        AuthResponse authResponse = userService.login(request);

        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getCurrentUser() {
        log.info("Get current user request");

        var user = userService.getCurrentUser();

        return ResponseEntity.ok(ApiResponse.success(user));
    }
}

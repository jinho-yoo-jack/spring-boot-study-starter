package com.study.myspringstudydiary.auth.service;

import com.study.myspringstudydiary.auth.repository.UserRepository;
import com.study.myspringstudydiary.auth.dto.*;
import com.study.myspringstudydiary.auth.entity.User;
import com.study.myspringstudydiary.auth.entity.UserRole;
import com.study.myspringstudydiary.auth.exception.AuthException;
import com.study.myspringstudydiary.auth.exception.InvalidTokenException;
import com.study.myspringstudydiary.study_log.exception.DuplicateResourceException;
import com.study.myspringstudydiary.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Authentication Service with JPA Repository
 * Handles login, signup, and token refresh operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 기본적으로 읽기 전용
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    // Refresh token 저장소 (임시 - 실제로는 별도 Entity/Table 권장)
    private static final Map<String, Long> refreshTokenStore = new HashMap<>();

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    /**
     * User login
     */
    @Transactional  // 쓰기 작업
    public LoginResponse login(LoginRequest request) {
        try {
            log.info("Login attempt for user: {}", request.getUsername());

            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Get user details (username 또는 email로 조회)
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseGet(() -> userRepository.findByEmail(request.getUsername())
                            .orElseThrow(() -> new AuthException("User not found")));

            // Generate tokens
            String roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));

            String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), roles);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

            // Save refresh token (임시 메모리 저장)
            refreshTokenStore.put(refreshToken, user.getId());

            log.info("Login successful for user: {}", user.getUsername());

            return LoginResponse.of(
                    accessToken,
                    refreshToken,
                    accessTokenValidity,
                    user.getUsername(),
                    user.getEmail()
            );

        } catch (AuthenticationException e) {
            log.error("Login failed for user: {}", request.getUsername(), e);
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    /**
     * User signup
     */
    @Transactional  // 쓰기 작업
    public SignupResponse signup(SignupRequest request) {
        log.info("Signup attempt for username: {}, email: {}", request.getUsername(), request.getEmail());

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + request.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        // Create new user
        User newUser = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())  // JPA @PrePersist를 사용할 수도 있음
                .build();

        // JPA Repository를 통한 저장
        User savedUser = userRepository.save(newUser);

        log.info("User registered successfully: {}", savedUser.getUsername());

        return SignupResponse.of(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail()
        );
    }

    /**
     * Refresh access token using refresh token
     */
    @Transactional  // 쓰기 작업
    public TokenResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        log.info("Token refresh attempt");

        // Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        // Check if it's a refresh token
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new InvalidTokenException("Token is not a refresh token");
        }

        // Find user by refresh token (임시 메모리에서 조회)
        Long userId = refreshTokenStore.get(refreshToken);
        if (userId == null) {
            throw new InvalidTokenException("Refresh token not found or expired");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidTokenException("User not found"));

        // Get user roles
        UserRole userRole = user.getRole() != null ? user.getRole() : UserRole.USER;
        String roles = "ROLE_" + userRole.name();

        // Generate new tokens
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), roles);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

        // Update refresh token in store
        refreshTokenStore.remove(refreshToken);
        refreshTokenStore.put(newRefreshToken, user.getId());

        log.info("Token refreshed successfully for user: {}", user.getUsername());

        return TokenResponse.of(
                newAccessToken,
                newRefreshToken,
                accessTokenValidity
        );
    }

    /**
     * Logout (invalidate refresh token)
     */
    @Transactional  // 쓰기 작업
    public void logout(String refreshToken) {
        log.info("Logout attempt");
        refreshTokenStore.remove(refreshToken);
        log.info("Logout successful");
    }

    /**
     * Get user profile
     */
    public User getUserProfile(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException("User not found: " + username));
    }

    /**
     * Update user password
     */
    @Transactional  // 쓰기 작업
    public void updatePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found"));

        String encodedPassword = passwordEncoder.encode(newPassword);
        userRepository.updatePassword(userId, encodedPassword, LocalDateTime.now());

        log.info("Password updated for user: {}", user.getUsername());
    }
}
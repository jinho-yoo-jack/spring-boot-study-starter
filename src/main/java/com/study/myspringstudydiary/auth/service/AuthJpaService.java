package com.study.myspringstudydiary.auth.service;

import com.study.myspringstudydiary.auth.dto.*;
import com.study.myspringstudydiary.auth.entity.RefreshToken;
import com.study.myspringstudydiary.auth.entity.User;
import com.study.myspringstudydiary.auth.entity.UserRole;
import com.study.myspringstudydiary.auth.exception.AuthException;
import com.study.myspringstudydiary.auth.exception.InvalidTokenException;
import com.study.myspringstudydiary.auth.repository.RefreshTokenRepository;
import com.study.myspringstudydiary.auth.repository.UserRepository;
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

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Authentication Service with JPA
 * Handles login, signup, and token refresh operations using JPA repositories
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthJpaService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    /**
     * User login
     */
    @Transactional
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

            // Get user details
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseGet(() -> userRepository.findByEmail(request.getUsername())
                            .orElseThrow(() -> new AuthException("User not found")));

            // Generate tokens
            String roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));

            String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), roles);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

            // Save refresh token to database
            Date refreshTokenExpiry = jwtTokenProvider.getExpirationFromToken(refreshToken);
            saveRefreshToken(user, refreshToken, refreshTokenExpiry);

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
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        log.info("Signup attempt for user: {}", request.getUsername());

        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        // Check if username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);

        log.info("Signup successful for user: {}", savedUser.getUsername());

        return SignupResponse.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .username(savedUser.getUsername())
                .message("Registration successful")
                .build();
    }

    /**
     * Refresh access token
     */
    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        String refreshTokenStr = request.getRefreshToken();
        log.info("Token refresh attempt");

        // Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshTokenStr)) {
            log.error("Invalid refresh token");
            throw new InvalidTokenException("Invalid refresh token");
        }

        // Find refresh token with user
        RefreshToken refreshToken = refreshTokenRepository
                .findValidTokenWithUser(refreshTokenStr, LocalDateTime.now())
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found or expired"));

        User user = refreshToken.getUser();

        // Get user role
        String role = "ROLE_" + user.getRole().name();

        // Generate new access token
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), role);

        log.info("Token refresh successful for user: {}", user.getUsername());

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenValidity)
                .build();
    }

    /**
     * Logout
     */
    @Transactional
    public void logout(String refreshToken) {
        log.info("Logout attempt");

        if (refreshToken != null && !refreshToken.isEmpty()) {
            // Delete refresh token
            refreshTokenRepository.deleteByToken(refreshToken);
            log.info("Logout successful - refresh token deleted");
        }
    }

    /**
     * Logout from all devices
     */
    @Transactional
    public void logoutAll(Long userId) {
        log.info("Logout all devices for user ID: {}", userId);

        // Delete all refresh tokens for the user
        refreshTokenRepository.deleteAllByUserId(userId);

        log.info("All refresh tokens deleted for user ID: {}", userId);
    }

    /**
     * Validate access token
     */
    public boolean validateAccessToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    /**
     * Get username from token
     */
    public String getUsernameFromToken(String token) {
        return jwtTokenProvider.getUsernameFromToken(token);
    }

    /**
     * Change password
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found"));

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Invalidate all refresh tokens (force re-login)
        refreshTokenRepository.deleteAllByUserId(userId);

        log.info("Password changed successfully for user: {}", user.getUsername());
    }

    /**
     * Clean up expired tokens
     */
    @Transactional
    public int cleanupExpiredTokens() {
        int deletedCount = refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Cleaned up {} expired refresh tokens", deletedCount);
        return deletedCount;
    }

    /**
     * Save refresh token
     */
    private void saveRefreshToken(User user, String tokenStr, Date expiresAt) {
        // Remove all existing refresh tokens for the user (optional: allow multiple sessions)
        // refreshTokenRepository.deleteAllByUserId(user.getId());

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenStr)
                .user(user)
                .expiresAt(new java.sql.Timestamp(expiresAt.getTime()).toLocalDateTime())
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    /**
     * Get user by ID with JPA
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found"));
    }

    /**
     * Get user by username with JPA
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException("User not found"));
    }
}
package com.study.myspringstudydiary.auth.service;

import com.study.myspringstudydiary.auth.dao.UserDao;
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

import java.sql.Timestamp;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Authentication Service
 * Handles login, signup, and token refresh operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserDao userDao;
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
            // Client 입력한 username과 password를 사용하여 인증(입력 값이 DB에 저장되어 있는 값과 일치하는지를 확인하는 역할)을 수행한다.
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Get user details
            User user = userDao.findByUsername(request.getUsername())
                    .orElseGet(() -> userDao.findByEmail(request.getUsername())
                            .orElseThrow(() -> new AuthException("User not found")));

            // Generate tokens
            String roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));

            String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), roles);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

            // Save refresh token to database
            Date refreshTokenExpiry = jwtTokenProvider.getExpirationFromToken(refreshToken);
            userDao.saveRefreshToken(user.getId(), refreshToken, new Timestamp(refreshTokenExpiry.getTime()));

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
        log.info("Signup attempt for username: {}, email: {}", request.getUsername(), request.getEmail());

        // Check if username already exists
        if (userDao.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + request.getUsername());
        }

        // Check if email already exists
        if (userDao.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        // Create new user
        // Password를 평문으로 저장을 하면 안된다.
        // BCryptPasswordEncoder를 사용하여 암호화된 비밀번호를 저장한다.
        User newUser = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .enabled(true)
                .build();

        User savedUser = userDao.save(newUser);

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
    @Transactional
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

        // Find user by refresh token
        User user = userDao.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found or expired"));

        // Get user roles
        UserRole userRole = user.getRole() != null ? user.getRole() : UserRole.USER;
        String roles = "ROLE_" + userRole.name();

        // Generate new tokens
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), roles);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

        // Update refresh token in database
        Date refreshTokenExpiry = jwtTokenProvider.getExpirationFromToken(newRefreshToken);
        userDao.saveRefreshToken(user.getId(), newRefreshToken, new Timestamp(refreshTokenExpiry.getTime()));

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
    @Transactional
    public void logout(String refreshToken) {
        log.info("Logout attempt");
        userDao.deleteRefreshToken(refreshToken);
        log.info("Logout successful");
    }
}

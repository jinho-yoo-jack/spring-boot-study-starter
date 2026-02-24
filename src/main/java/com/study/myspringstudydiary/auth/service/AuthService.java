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
            log.info("로그인 시도: username={}", request.getUsername());
            log.debug("인증 프로세스 시작: username={}", request.getUsername());

            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            log.debug("인증 성공: username={}", request.getUsername());

            // Get user details
            User user = userDao.findByUsername(request.getUsername())
                    .orElseGet(() -> userDao.findByEmail(request.getUsername())
                            .orElseThrow(() -> new AuthException("User not found")));
            log.debug("사용자 정보 조회 완료: userId={}, role={}", user.getId(), user.getRole());

            // Generate tokens
            String roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));
            log.debug("사용자 권한: roles={}", roles);

            String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), roles);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
            log.debug("토큰 생성 완료: accessTokenLength={}, refreshTokenLength={}",
                    accessToken.length(), refreshToken.length());

            // Save refresh token to database
            Date refreshTokenExpiry = jwtTokenProvider.getExpirationFromToken(refreshToken);
            userDao.saveRefreshToken(user.getId(), refreshToken, new Timestamp(refreshTokenExpiry.getTime()));
            log.debug("리프레시 토큰 저장 완료: userId={}, expiryTime={}", user.getId(), refreshTokenExpiry);

            log.info("로그인 성공: username={}, userId={}", user.getUsername(), user.getId());

            return LoginResponse.of(
                    accessToken,
                    refreshToken,
                    accessTokenValidity,
                    user.getUsername(),
                    user.getEmail()
            );

        } catch (AuthenticationException e) {
            log.error("로그인 실패: username={}, error={}", request.getUsername(), e.getMessage());
            throw new BadCredentialsException("Invalid username or password");
        } catch (Exception e) {
            log.error("로그인 처리 중 예외 발생: username={}", request.getUsername(), e);
            throw e;
        }
    }

    /**
     * User signup
     */
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        log.info("회원가입 시도: username={}, email={}", request.getUsername(), request.getEmail());

        // Check if username already exists
        log.debug("사용자명 중복 확인: username={}", request.getUsername());
        if (userDao.existsByUsername(request.getUsername())) {
            log.warn("회원가입 실패 - 사용자명 중복: username={}", request.getUsername());
            throw new DuplicateResourceException("Username already exists: " + request.getUsername());
        }

        // Check if email already exists
        log.debug("이메일 중복 확인: email={}", request.getEmail());
        if (userDao.existsByEmail(request.getEmail())) {
            log.warn("회원가입 실패 - 이메일 중복: email={}", request.getEmail());
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        // Create new user
        log.debug("새 사용자 생성 중: username={}", request.getUsername());
        User newUser = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .enabled(true)
                .build();

        User savedUser = userDao.save(newUser);
        log.debug("사용자 DB 저장 완료: userId={}", savedUser.getId());

        log.info("회원가입 성공: username={}, userId={}, email={}",
                savedUser.getUsername(), savedUser.getId(), savedUser.getEmail());

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

        log.info("토큰 갱신 시도");
        log.debug("리프레시 토큰 검증 시작");

        // Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("토큰 갱신 실패 - 유효하지 않은 리프레시 토큰");
            throw new InvalidTokenException("Invalid refresh token");
        }

        // Check if it's a refresh token
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            log.warn("토큰 갱신 실패 - 리프레시 토큰이 아님");
            throw new InvalidTokenException("Token is not a refresh token");
        }
        log.debug("리프레시 토큰 검증 완료");

        // Find user by refresh token
        User user = userDao.findByRefreshToken(refreshToken)
                .orElseThrow(() -> {
                    log.warn("토큰 갱신 실패 - 토큰이 DB에 없거나 만료됨");
                    return new InvalidTokenException("Refresh token not found or expired");
                });
        log.debug("사용자 조회 완료: userId={}, username={}", user.getId(), user.getUsername());

        // Get user roles
        UserRole userRole = user.getRole() != null ? user.getRole() : UserRole.USER;
        String roles = "ROLE_" + userRole.name();
        log.debug("사용자 권한 확인: roles={}", roles);

        // Generate new tokens
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), roles);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
        log.debug("새 토큰 생성 완료");

        // Update refresh token in database
        Date refreshTokenExpiry = jwtTokenProvider.getExpirationFromToken(newRefreshToken);
        userDao.saveRefreshToken(user.getId(), newRefreshToken, new Timestamp(refreshTokenExpiry.getTime()));
        log.debug("새 리프레시 토큰 DB 저장 완료: expiryTime={}", refreshTokenExpiry);

        log.info("토큰 갱신 성공: username={}", user.getUsername());

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
        log.info("로그아웃 시도");
        log.debug("리프레시 토큰 삭제 중: tokenLength={}", refreshToken != null ? refreshToken.length() : 0);

        try {
            userDao.deleteRefreshToken(refreshToken);
            log.info("로그아웃 성공 - 리프레시 토큰 삭제 완료");
        } catch (Exception e) {
            log.warn("로그아웃 중 오류 발생 (토큰이 이미 없을 수 있음)", e);
            // 로그아웃은 실패해도 문제없으므로 예외를 던지지 않음
        }
    }
}

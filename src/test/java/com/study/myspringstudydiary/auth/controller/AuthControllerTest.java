package com.study.myspringstudydiary.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.myspringstudydiary.auth.dto.*;
import com.study.myspringstudydiary.auth.service.AuthService;
import com.study.myspringstudydiary.global.security.config.SecurityConfig;
import com.study.myspringstudydiary.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
@DisplayName("Auth Controller 테스트")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private LoginRequest loginRequest;
    private LoginResponse loginResponse;
    private SignupRequest signupRequest;
    private SignupResponse signupResponse;
    private RefreshTokenRequest refreshRequest;
    private TokenResponse tokenResponse;

    @BeforeEach
    void setUp() {
        // Login 테스트 데이터
        loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        loginResponse = LoginResponse.builder()
                .accessToken("access.token.here")
                .refreshToken("refresh.token.here")
                .tokenType("Bearer")
                .expiresIn(1800L)
                .build();

        // Signup 테스트 데이터
        signupRequest = SignupRequest.builder()
                .username("newuser")
                .password("Password123!")
                .email("newuser@test.com")
                .build();

        signupResponse = SignupResponse.builder()
                .userId(1L)
                .username("newuser")
                .email("newuser@test.com")
                .message("User registered successfully")
                .build();

        // Refresh 테스트 데이터
        refreshRequest = RefreshTokenRequest.builder()
                .refreshToken("refresh.token.here")
                .build();

        tokenResponse = TokenResponse.builder()
                .accessToken("new.access.token")
                .tokenType("Bearer")
                .expiresIn(1800L)
                .build();
    }

    @Test
    @DisplayName("로그인 - 성공")
    void login_Success() throws Exception {
        // Given
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access.token.here"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh.token.here"));
    }

    @Test
    @DisplayName("로그인 - 유효성 검증 실패")
    void login_ValidationFailed() throws Exception {
        // Given
        LoginRequest invalidRequest = LoginRequest.builder()
                .username("")  // 빈 username
                .password("password123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입 - 성공")
    void signup_Success() throws Exception {
        // Given
        when(authService.signup(any(SignupRequest.class))).thenReturn(signupResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("newuser"))
                .andExpect(jsonPath("$.data.email").value("newuser@test.com"));
    }

    @Test
    @DisplayName("회원가입 - 이메일 형식 오류")
    void signup_InvalidEmail() throws Exception {
        // Given
        SignupRequest invalidRequest = SignupRequest.builder()
                .username("newuser")
                .password("Password123!")
                .email("invalid-email")  // 잘못된 이메일 형식
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("토큰 갱신 - 성공")
    void refresh_Success() throws Exception {
        // Given
        when(authService.refresh(any(RefreshTokenRequest.class))).thenReturn(tokenResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new.access.token"));
    }

    @Test
    @DisplayName("로그아웃 - 성공")
    void logout_Success() throws Exception {
        // Given
        doNothing().when(authService).logout(any(String.class));

        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Logged out successfully"));
    }
}
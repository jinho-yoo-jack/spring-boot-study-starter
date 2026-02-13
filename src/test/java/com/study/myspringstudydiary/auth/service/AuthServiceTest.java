package com.study.myspringstudydiary.auth.service;

import com.study.myspringstudydiary.auth.dao.UserDao;
import com.study.myspringstudydiary.auth.dto.*;
import com.study.myspringstudydiary.auth.entity.User;
import com.study.myspringstudydiary.auth.entity.UserRole;
import com.study.myspringstudydiary.auth.exception.InvalidTokenException;
import com.study.myspringstudydiary.global.security.jwt.JwtTokenProvider;
import com.study.myspringstudydiary.study_log.exception.DuplicateResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Mock
    private Authentication authentication;

    private User testUser;
    private final long accessTokenValidity = 1800L;
    private final long refreshTokenValidity = 604800L;

    @BeforeEach
    void setUp() {
        // 테스트용 User 객체 생성
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("testuser@test.com")
                .password("encodedPassword")
                .role(UserRole.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        // AuthService에 @Value 필드 주입
        ReflectionTestUtils.setField(authService, "accessTokenValidity", accessTokenValidity);
        ReflectionTestUtils.setField(authService, "refreshTokenValidity", refreshTokenValidity);
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTests {

        @Test
        @DisplayName("로그인 성공 - username으로 로그인")
        void login_WithUsername_Success() {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .username("testuser")
                    .password("password123")
                    .build();

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);

            doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                    .when(authentication).getAuthorities();

            when(userDao.findByUsername("testuser"))
                    .thenReturn(Optional.of(testUser));

            when(jwtTokenProvider.generateAccessToken("testuser", "ROLE_USER"))
                    .thenReturn("access.token.here");

            when(jwtTokenProvider.generateRefreshToken("testuser"))
                    .thenReturn("refresh.token.here");

            when(jwtTokenProvider.getExpirationFromToken("refresh.token.here"))
                    .thenReturn(new Date(System.currentTimeMillis() + 604800000));

            // When
            LoginResponse response = authService.login(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("access.token.here");
            assertThat(response.getRefreshToken()).isEqualTo("refresh.token.here");
            assertThat(response.getUsername()).isEqualTo("testuser");
            assertThat(response.getEmail()).isEqualTo("testuser@test.com");

            verify(userDao).saveRefreshToken(eq(1L), eq("refresh.token.here"), any(Timestamp.class));
        }

        @Test
        @DisplayName("로그인 성공 - email로 로그인")
        void login_WithEmail_Success() {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .username("testuser@test.com")
                    .password("password123")
                    .build();

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);

            doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                    .when(authentication).getAuthorities();

            when(userDao.findByUsername("testuser@test.com"))
                    .thenReturn(Optional.empty());

            when(userDao.findByEmail("testuser@test.com"))
                    .thenReturn(Optional.of(testUser));

            when(jwtTokenProvider.generateAccessToken("testuser", "ROLE_USER"))
                    .thenReturn("access.token.here");

            when(jwtTokenProvider.generateRefreshToken("testuser"))
                    .thenReturn("refresh.token.here");

            when(jwtTokenProvider.getExpirationFromToken("refresh.token.here"))
                    .thenReturn(new Date(System.currentTimeMillis() + 604800000));

            // When
            LoginResponse response = authService.login(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getUsername()).isEqualTo("testuser");
            assertThat(response.getEmail()).isEqualTo("testuser@test.com");

            verify(userDao).findByUsername("testuser@test.com");
            verify(userDao).findByEmail("testuser@test.com");
        }

        @Test
        @DisplayName("로그인 실패 - 잘못된 credentials")
        void login_InvalidCredentials_ThrowsException() {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .username("testuser")
                    .password("wrongpassword")
                    .build();

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // When & Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("Invalid username or password");

            verify(userDao, never()).saveRefreshToken(anyLong(), anyString(), any(Timestamp.class));
        }
    }

    @Nested
    @DisplayName("회원가입 테스트")
    class SignupTests {

        @Test
        @DisplayName("회원가입 성공")
        void signup_Success() {
            // Given
            SignupRequest request = SignupRequest.builder()
                    .username("newuser")
                    .email("newuser@test.com")
                    .password("Password123!")
                    .build();

            when(userDao.existsByUsername("newuser")).thenReturn(false);
            when(userDao.existsByEmail("newuser@test.com")).thenReturn(false);
            when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");

            User newUser = User.builder()
                    .id(2L)
                    .username("newuser")
                    .email("newuser@test.com")
                    .password("encodedPassword")
                    .role(UserRole.USER)
                    .enabled(true)
                    .build();

            when(userDao.save(any(User.class))).thenReturn(newUser);

            // When
            SignupResponse response = authService.signup(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getUserId()).isEqualTo(2L);
            assertThat(response.getUsername()).isEqualTo("newuser");
            assertThat(response.getEmail()).isEqualTo("newuser@test.com");

            verify(userDao).existsByUsername("newuser");
            verify(userDao).existsByEmail("newuser@test.com");
            verify(passwordEncoder).encode("Password123!");
            verify(userDao).save(any(User.class));
        }

        @Test
        @DisplayName("회원가입 실패 - 중복된 username")
        void signup_DuplicateUsername_ThrowsException() {
            // Given
            SignupRequest request = SignupRequest.builder()
                    .username("existinguser")
                    .email("newuser@test.com")
                    .password("Password123!")
                    .build();

            when(userDao.existsByUsername("existinguser")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.signup(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessage("Username already exists: existinguser");

            verify(userDao, never()).save(any(User.class));
        }

        @Test
        @DisplayName("회원가입 실패 - 중복된 email")
        void signup_DuplicateEmail_ThrowsException() {
            // Given
            SignupRequest request = SignupRequest.builder()
                    .username("newuser")
                    .email("existing@test.com")
                    .password("Password123!")
                    .build();

            when(userDao.existsByUsername("newuser")).thenReturn(false);
            when(userDao.existsByEmail("existing@test.com")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.signup(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessage("Email already exists: existing@test.com");

            verify(userDao, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("토큰 갱신 테스트")
    class RefreshTests {

        @Test
        @DisplayName("토큰 갱신 성공")
        void refresh_Success() {
            // Given
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("valid.refresh.token")
                    .build();

            when(jwtTokenProvider.validateToken("valid.refresh.token")).thenReturn(true);
            when(jwtTokenProvider.isRefreshToken("valid.refresh.token")).thenReturn(true);
            when(userDao.findByRefreshToken("valid.refresh.token")).thenReturn(Optional.of(testUser));
            when(jwtTokenProvider.generateAccessToken("testuser", "ROLE_USER"))
                    .thenReturn("new.access.token");
            when(jwtTokenProvider.generateRefreshToken("testuser"))
                    .thenReturn("new.refresh.token");
            when(jwtTokenProvider.getExpirationFromToken("new.refresh.token"))
                    .thenReturn(new Date(System.currentTimeMillis() + 604800000));

            // When
            TokenResponse response = authService.refresh(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("new.access.token");
            assertThat(response.getRefreshToken()).isEqualTo("new.refresh.token");
            assertThat(response.getExpiresIn()).isEqualTo(accessTokenValidity);

            verify(userDao).saveRefreshToken(eq(1L), eq("new.refresh.token"), any(Timestamp.class));
        }

        @Test
        @DisplayName("토큰 갱신 실패 - 유효하지 않은 토큰")
        void refresh_InvalidToken_ThrowsException() {
            // Given
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("invalid.refresh.token")
                    .build();

            when(jwtTokenProvider.validateToken("invalid.refresh.token")).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> authService.refresh(request))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessage("Invalid refresh token");

            verify(userDao, never()).findByRefreshToken(anyString());
        }

        @Test
        @DisplayName("토큰 갱신 실패 - Access Token으로 갱신 시도")
        void refresh_NotRefreshToken_ThrowsException() {
            // Given
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("access.token.here")
                    .build();

            when(jwtTokenProvider.validateToken("access.token.here")).thenReturn(true);
            when(jwtTokenProvider.isRefreshToken("access.token.here")).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> authService.refresh(request))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessage("Token is not a refresh token");

            verify(userDao, never()).findByRefreshToken(anyString());
        }

        @Test
        @DisplayName("토큰 갱신 실패 - 토큰이 DB에 없음")
        void refresh_TokenNotInDb_ThrowsException() {
            // Given
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("valid.but.not.in.db")
                    .build();

            when(jwtTokenProvider.validateToken("valid.but.not.in.db")).thenReturn(true);
            when(jwtTokenProvider.isRefreshToken("valid.but.not.in.db")).thenReturn(true);
            when(userDao.findByRefreshToken("valid.but.not.in.db")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.refresh(request))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessage("Refresh token not found or expired");

            verify(userDao, never()).saveRefreshToken(anyLong(), anyString(), any(Timestamp.class));
        }

        @Test
        @DisplayName("토큰 갱신 - null role 처리")
        void refresh_NullRole_DefaultsToUser() {
            // Given
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("valid.refresh.token")
                    .build();

            User userWithNullRole = User.builder()
                    .id(1L)
                    .username("testuser")
                    .email("testuser@test.com")
                    .password("encodedPassword")
                    .role(null) // null role
                    .enabled(true)
                    .build();

            when(jwtTokenProvider.validateToken("valid.refresh.token")).thenReturn(true);
            when(jwtTokenProvider.isRefreshToken("valid.refresh.token")).thenReturn(true);
            when(userDao.findByRefreshToken("valid.refresh.token")).thenReturn(Optional.of(userWithNullRole));
            when(jwtTokenProvider.generateAccessToken("testuser", "ROLE_USER"))
                    .thenReturn("new.access.token");
            when(jwtTokenProvider.generateRefreshToken("testuser"))
                    .thenReturn("new.refresh.token");
            when(jwtTokenProvider.getExpirationFromToken("new.refresh.token"))
                    .thenReturn(new Date(System.currentTimeMillis() + 604800000));

            // When
            TokenResponse response = authService.refresh(request);

            // Then
            assertThat(response).isNotNull();
            verify(jwtTokenProvider).generateAccessToken("testuser", "ROLE_USER");
        }
    }

    @Nested
    @DisplayName("로그아웃 테스트")
    class LogoutTests {

        @Test
        @DisplayName("로그아웃 성공")
        void logout_Success() {
            // Given
            String refreshToken = "refresh.token.to.logout";

            // When
            authService.logout(refreshToken);

            // Then
            verify(userDao).deleteRefreshToken(refreshToken);
        }

        @Test
        @DisplayName("로그아웃 - null 토큰도 처리")
        void logout_NullToken_StillCallsDao() {
            // Given
            String refreshToken = null;

            // When
            authService.logout(refreshToken);

            // Then
            verify(userDao).deleteRefreshToken(null);
        }
    }
}
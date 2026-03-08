package com.study.myspringstudydiary.global.security.jwt;

import com.study.myspringstudydiary.auth.exception.ExpiredTokenException;
import com.study.myspringstudydiary.auth.exception.InvalidTokenException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtTokenProvider 단위 테스트")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secretString = "ThisIsAVerySecureSecretKeyForJWTTestingPurposesThatIsLongEnough";
    private final long accessTokenValidity = 1800; // 30 minutes in seconds
    private final long refreshTokenValidity = 604800; // 7 days in seconds

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                secretString,
                accessTokenValidity,
                refreshTokenValidity
        );
    }

    @Nested
    @DisplayName("토큰 생성 테스트")
    class TokenGenerationTests {

        @Nested
        @DisplayName("Access 토큰 생성 테스트")
        class AccessTokenGenerationTests {}

        @Nested
        @DisplayName("Refresh 토큰 생성 테스트")
        class RefreshTokenGenerationTests {}

        @Test
        @DisplayName("Authentication으로 Access Token 생성 - 성공")
        void generateAccessToken_FromAuthentication_Success() {
            // Given
            UserDetails userDetails = User.builder()
                    .username("testuser")
                    .password("password")
                    .authorities("ROLE_USER")
                    .build();

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );

            // When
            String token = jwtTokenProvider.generateAccessToken(authentication);

            // Then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // JWT는 3부분으로 구성
        }

        @Test
        @DisplayName("username과 roles로 Access Token 생성 - 성공")
        void generateAccessToken_FromUsernameAndRoles_Success() {
            // Given
            String username = "testuser";
            String roles = "ROLE_USER,ROLE_ADMIN";

            // When
            String token = jwtTokenProvider.generateAccessToken(username, roles);

            // Then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();

            // 생성된 토큰에서 username 확인
            String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);
            assertThat(extractedUsername).isEqualTo(username);

            // 생성된 토큰에서 roles 확인
            String extractedRoles = jwtTokenProvider.getRolesFromToken(token);
            assertThat(extractedRoles).isEqualTo(roles);
        }

        @Test
        @DisplayName("Refresh Token 생성 - 성공")
        void generateRefreshToken_Success() {
            // Given
            String username = "testuser";

            // When
            String token = jwtTokenProvider.generateRefreshToken(username);

            // Then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();

            // Refresh Token인지 확인
            assertThat(jwtTokenProvider.isRefreshToken(token)).isTrue();

            // username 확인
            String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);
            assertThat(extractedUsername).isEqualTo(username);
        }
    }

    @Nested
    @DisplayName("토큰 파싱 테스트")
    class TokenParsingTests {

        @Test
        @DisplayName("토큰에서 username 추출 - 성공")
        void getUsernameFromToken_Success() {
            // Given
            String username = "testuser";
            String token = jwtTokenProvider.generateAccessToken(username, "ROLE_USER");

            // When
            String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

            // Then
            assertThat(extractedUsername).isEqualTo(username);
        }

        @Test
        @DisplayName("토큰에서 roles 추출 - 성공")
        void getRolesFromToken_Success() {
            // Given
            String username = "testuser";
            String roles = "ROLE_USER,ROLE_ADMIN";
            String token = jwtTokenProvider.generateAccessToken(username, roles);

            // When
            String extractedRoles = jwtTokenProvider.getRolesFromToken(token);

            // Then
            assertThat(extractedRoles).isEqualTo(roles);
        }

        @Test
        @DisplayName("잘못된 토큰에서 username 추출 - 실패")
        void getUsernameFromToken_InvalidToken_ThrowsException() {
            // Given
            String invalidToken = "invalid.token.here";

            // When & Then
            assertThatThrownBy(() -> jwtTokenProvider.getUsernameFromToken(invalidToken))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessage("Invalid token");
        }

        @Test
        @DisplayName("토큰 만료 시간 추출 - 성공")
        void getExpirationFromToken_Success() {
            // Given
            String token = jwtTokenProvider.generateAccessToken("testuser", "ROLE_USER");

            // When
            Date expiration = jwtTokenProvider.getExpirationFromToken(token);

            // Then
            assertThat(expiration).isNotNull();
            assertThat(expiration).isAfter(new Date());

            // 만료 시간이 약 30분 후인지 확인 (오차 1분 허용)
            long expectedExpiry = System.currentTimeMillis() + (accessTokenValidity * 1000);
            assertThat(expiration.getTime()).isBetween(
                    expectedExpiry - 60000, // 1분 전
                    expectedExpiry + 60000  // 1분 후
            );
        }
    }

    @Nested
    @DisplayName("토큰 검증 테스트")
    class TokenValidationTests {

        @Test
        @DisplayName("유효한 토큰 검증 - 성공")
        void validateToken_Valid_ReturnsTrue() {
            // Given
            String token = jwtTokenProvider.generateAccessToken("testuser", "ROLE_USER");

            // When
            boolean isValid = jwtTokenProvider.validateToken(token);

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("만료된 토큰 검증 - ExpiredTokenException 발생")
        void validateToken_Expired_ThrowsException() {
            // Given - 이미 만료된 토큰 생성
            SecretKey key = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
            String expiredToken = Jwts.builder()
                    .subject("testuser")
                    .claim("type", "access")
                    .issuedAt(new Date(System.currentTimeMillis() - 3600000)) // 1시간 전
                    .expiration(new Date(System.currentTimeMillis() - 1800000)) // 30분 전 만료
                    .signWith(key)
                    .compact();

            // When & Then
            assertThatThrownBy(() -> jwtTokenProvider.validateToken(expiredToken))
                    .isInstanceOf(ExpiredTokenException.class)
                    .hasMessage("Token has expired");
        }

        @Test
        @DisplayName("잘못된 형식의 토큰 검증 - InvalidTokenException 발생")
        void validateToken_Malformed_ThrowsException() {
            // Given
            String malformedToken = "this.is.not.a.valid.jwt.token";

            // When & Then
            assertThatThrownBy(() -> jwtTokenProvider.validateToken(malformedToken))
                    .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        @DisplayName("잘못된 서명의 토큰 검증 - InvalidTokenException 발생")
        void validateToken_InvalidSignature_ThrowsException() {
            // Given - 다른 키로 서명된 토큰
            SecretKey wrongKey = Keys.hmacShaKeyFor("AnotherSecretKeyThatIsDifferentFromTheOriginalOneAndLongEnough".getBytes());
            String tokenWithWrongSignature = Jwts.builder()
                    .subject("testuser")
                    .claim("type", "access")
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 3600000))
                    .signWith(wrongKey)
                    .compact();

            // When & Then
            assertThatThrownBy(() -> jwtTokenProvider.validateToken(tokenWithWrongSignature))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessage("Invalid JWT signature");
        }

        @Test
        @DisplayName("빈 토큰 검증 - InvalidTokenException 발생")
        void validateToken_Empty_ThrowsException() {
            // Given
            String emptyToken = "";

            // When & Then
            assertThatThrownBy(() -> jwtTokenProvider.validateToken(emptyToken))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessage("JWT claims string is empty");
        }
    }

    @Nested
    @DisplayName("토큰 타입 검증 테스트")
    class TokenTypeTests {

        @Test
        @DisplayName("Refresh Token 타입 확인 - 성공")
        void isRefreshToken_Valid_ReturnsTrue() {
            // Given
            String refreshToken = jwtTokenProvider.generateRefreshToken("testuser");

            // When
            boolean isRefresh = jwtTokenProvider.isRefreshToken(refreshToken);

            // Then
            assertThat(isRefresh).isTrue();
        }

        @Test
        @DisplayName("Access Token을 Refresh Token으로 확인 - 실패")
        void isRefreshToken_AccessToken_ReturnsFalse() {
            // Given
            String accessToken = jwtTokenProvider.generateAccessToken("testuser", "ROLE_USER");

            // When
            boolean isRefresh = jwtTokenProvider.isRefreshToken(accessToken);

            // Then
            assertThat(isRefresh).isFalse();
        }

        @Test
        @DisplayName("잘못된 토큰 타입 확인 - false 반환")
        void isRefreshToken_InvalidToken_ReturnsFalse() {
            // Given
            String invalidToken = "invalid.token.here";

            // When
            boolean isRefresh = jwtTokenProvider.isRefreshToken(invalidToken);

            // Then
            assertThat(isRefresh).isFalse();
        }
    }

    @Nested
    @DisplayName("토큰 만료 시간 테스트")
    class TokenExpiryTests {

        @Test
        @DisplayName("Access Token 만료 시간 확인")
        void accessToken_ExpiryTime_Correct() {
            // Given
            String token = jwtTokenProvider.generateAccessToken("testuser", "ROLE_USER");

            // When
            Date expiration = jwtTokenProvider.getExpirationFromToken(token);
            long expiryTimeInMillis = expiration.getTime() - System.currentTimeMillis();

            // Then
            // 30분(1800초)의 오차 범위 1분 이내
            assertThat(expiryTimeInMillis).isBetween(
                    (accessTokenValidity * 1000) - 60000,
                    (accessTokenValidity * 1000) + 60000
            );
        }

        @Test
        @DisplayName("Refresh Token 만료 시간 확인")
        void refreshToken_ExpiryTime_Correct() {
            // Given
            String token = jwtTokenProvider.generateRefreshToken("testuser");

            // When
            Date expiration = jwtTokenProvider.getExpirationFromToken(token);
            long expiryTimeInMillis = expiration.getTime() - System.currentTimeMillis();

            // Then
            // 7일(604800초)의 오차 범위 1분 이내
            assertThat(expiryTimeInMillis).isBetween(
                    (refreshTokenValidity * 1000) - 60000,
                    (refreshTokenValidity * 1000) + 60000
            );
        }
    }
}
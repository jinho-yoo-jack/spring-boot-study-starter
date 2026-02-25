package com.study.myspringstudydiary.global.security.jwt;

import com.study.myspringstudydiary.auth.exception.ExpiredTokenException;
import com.study.myspringstudydiary.auth.exception.InvalidTokenException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Authentication Filter
 * Extends OncePerRequestFilter to execute once per request
 * Extracts JWT from Authorization header and authenticates the user
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // Extract JWT token from request
            // eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsInJvbGVzIjoiUk9MRV9VU0VSIiwidHlwZSI6ImFjY2VzcyIsImlhdCI6MTc3MjAxOTYwMSwiZXhwIjoxNzcyMDIxNDAxfQ.0KYXXfzXsae7kG4ke51peySPqEQqtPgqSo4eJ3-MsjnGC8r_d8qXJKrC0JwzHeLzt8uh5MG761o2heO0KeqlAg
            String token = extractTokenFromRequest(request);

            // If token exists and is valid, authenticate the user
            if (token != null) {
                authenticateUser(token, request);
            }
        } catch (ExpiredTokenException | InvalidTokenException e) {
            // Log the exception but continue the filter chain
            // The exception will be handled by JwtAuthenticationEntryPoint
            log.error("JWT authentication failed: {}", e.getMessage());
            request.setAttribute("exception", e);
        } catch (Exception e) {
            log.error("Unexpected error during JWT authentication", e);
            request.setAttribute("exception", e);
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/");
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        // Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsInJvbGVzIjoiUk9MRV9VU0VSIiwidHlwZSI6ImFjY2VzcyIsImlhdCI6MTc3MjAxOTYwMSwiZXhwIjoxNzcyMDIxNDAxfQ.0KYXXfzXsae7kG4ke51peySPqEQqtPgqSo4eJ3-MsjnGC8r_d8qXJKrC0JwzHeLzt8uh5MG761o2heO0KeqlAg

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    /**
     * Authenticate user using JWT token
     */
    private void authenticateUser(String token, HttpServletRequest request) {
        // Validate token
        if (jwtTokenProvider.validateToken(token)) {
            // Extract username from token
            // 로그인할 때, Access Token을 만들 때, 입력했던 or 넣었던 값들 중에
            // username 꺼내기 메서드
            String username = jwtTokenProvider.getUsernameFromToken(token);

            // Extract roles from token
            // Role(권한 정보) 꺼내기 메서드
            String rolesString = jwtTokenProvider.getRolesFromToken(token);
            List<SimpleGrantedAuthority> authorities = parseAuthorities(rolesString);

            // Create UserDetails
            // Spring Security에서 사용하는 UserDetails 인터페이스를 구현한 User 객체 생성
            UserDetails userDetails = User.builder()
                    .username(username)
                    .password("") // Password is not needed for JWT authentication
                    .authorities(authorities)
                    .build();

            // Create authentication token
            // 사용자 정보 or 인증된 사용자의 정보를 담고 있는 DTO
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            // Set additional details
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Set authentication in SecurityContext
            // SecurityContext에 저장한다.
            // 이렇게 저장을 해주는 이유는 -> "Spring Boot 영역에서 비즈니스 로직을 실행할 때, 유저의 정보가 필요한 경우,
            // 손쉽게 빼서 사용할 수 있도록 하기 위해서"
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Authenticated user: {}", username);
        }
    }

    /**
     * Parse roles string into authorities
     */
    private List<SimpleGrantedAuthority> parseAuthorities(String rolesString) {
        if (rolesString == null || rolesString.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.stream(rolesString.split(","))
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}

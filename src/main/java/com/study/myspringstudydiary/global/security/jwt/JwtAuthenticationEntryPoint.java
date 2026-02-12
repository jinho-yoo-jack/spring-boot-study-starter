package com.study.myspringstudydiary.global.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.myspringstudydiary.auth.exception.ExpiredTokenException;
import com.study.myspringstudydiary.auth.exception.InvalidTokenException;
import com.study.myspringstudydiary.global.common.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT Authentication Entry Point
 * Handles unauthorized access attempts
 * Returns JSON response instead of redirecting to login page
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        log.error("Unauthorized access attempt: {}", authException.getMessage());

        // Check if there's an exception set by the filter
        Exception exception = (Exception) request.getAttribute("exception");

        String errorCode;
        String errorMessage;

        if (exception instanceof ExpiredTokenException) {
            errorCode = "TOKEN_EXPIRED";
            errorMessage = exception.getMessage();
        } else if (exception instanceof InvalidTokenException) {
            errorCode = "INVALID_TOKEN";
            errorMessage = exception.getMessage();
        } else {
            errorCode = "UNAUTHORIZED";
            errorMessage = "Authentication is required to access this resource";
        }

        // Create error response
        ApiResponse<Void> apiResponse = ApiResponse.error(errorCode, errorMessage);

        // Set response properties
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Write JSON response
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}

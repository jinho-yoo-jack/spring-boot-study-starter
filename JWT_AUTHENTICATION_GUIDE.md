# JWT Authentication System - Implementation Guide

## Overview

This document describes the complete JWT authentication system implemented for the Spring Boot application. The system provides secure, stateless authentication using JSON Web Tokens (JWT) with a two-token approach (access token and refresh token).

## Architecture

### Technology Stack
- **Spring Boot 3.x**
- **Spring Security 6.x**
- **JJWT 0.12.3** (JSON Web Token library)
- **JdbcTemplate** for database operations
- **BCrypt** for password hashing
- **MySQL** database

### Authentication Flow

1. **User Registration (Signup)**
   - User provides username, email, password
   - Password is hashed using BCrypt
   - User is stored in database with role `ROLE_USER`

2. **User Login**
   - User provides username/email and password
   - Credentials are validated
   - Access token (30 min) and refresh token (7 days) are generated
   - Refresh token is stored in database

3. **API Access**
   - Client includes access token in `Authorization: Bearer <token>` header
   - JWT filter validates token and sets authentication context
   - Protected endpoints are accessible

4. **Token Refresh**
   - When access token expires, client uses refresh token
   - New access token and refresh token are generated
   - Old refresh token is invalidated

## File Structure

```
src/main/java/com/study/myspringstudydiary/
├── security/
│   ├── config/
│   │   └── SecurityConfig.java              # Security configuration
│   └── jwt/
│       ├── JwtTokenProvider.java            # Token generation & validation
│       ├── JwtAuthenticationFilter.java     # JWT filter for requests
│       └── JwtAuthenticationEntryPoint.java # Unauthorized error handler
├── auth/
│   ├── controller/
│   │   └── AuthController.java              # REST endpoints
│   ├── service/
│   │   ├── AuthService.java                 # Business logic
│   │   └── CustomUserDetailsService.java   # User loading
│   ├── dao/
│   │   └── UserDao.java                     # Database operations
│   ├── entity/
│   │   ├── User.java                        # User entity
│   │   └── UserRole.java                    # Role enum
│   ├── dto/
│   │   ├── LoginRequest.java
│   │   ├── LoginResponse.java
│   │   ├── SignupRequest.java
│   │   ├── SignupResponse.java
│   │   ├── RefreshTokenRequest.java
│   │   └── TokenResponse.java
│   └── exception/
│       ├── AuthException.java
│       ├── InvalidTokenException.java
│       └── ExpiredTokenException.java
└── global/
    └── exception/
        └── GlobalExceptionHandler.java      # Updated with auth exceptions
```

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    username VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Refresh Tokens Table
```sql
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

## API Endpoints

### 1. User Signup
**Endpoint:** `POST /api/auth/signup`

**Request Body:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123",
  "fullName": "John Doe"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "message": "User registered successfully"
  },
  "errorCode": null,
  "errorMessage": null
}
```

### 2. User Login
**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "username": "johndoe",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 1800,
    "username": "johndoe",
    "email": "john@example.com"
  },
  "errorCode": null,
  "errorMessage": null
}
```

### 3. Refresh Token
**Endpoint:** `POST /api/auth/refresh`

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 1800
  },
  "errorCode": null,
  "errorMessage": null
}
```

### 4. Logout
**Endpoint:** `POST /api/auth/logout`

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": "Logged out successfully",
  "errorCode": null,
  "errorMessage": null
}
```

### Protected Endpoints
All other endpoints require authentication. Include the access token in the `Authorization` header:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Configuration

### application.yml
```yaml
jwt:
  secret: ${JWT_SECRET:myVerySecretKeyForJwtTokenGenerationAndValidation123456789012345678901234567890}
  access-token-validity: 1800  # 30 minutes in seconds
  refresh-token-validity: 604800  # 7 days in seconds
```

### Environment Variables (Production)
For production, set the JWT secret as an environment variable:
```bash
export JWT_SECRET=your-very-secure-secret-key-here-at-least-64-characters-long
```

## Security Features

1. **Stateless Authentication**
   - No server-side session storage
   - JWT tokens contain all necessary information
   - Scalable across multiple servers

2. **Password Security**
   - BCrypt hashing with salt
   - Passwords never stored in plain text

3. **Token Security**
   - Access tokens expire after 30 minutes
   - Refresh tokens expire after 7 days
   - Refresh tokens stored in database and can be revoked

4. **CSRF Protection**
   - Disabled for REST API (stateless)
   - JWT tokens in headers prevent CSRF

5. **Exception Handling**
   - Global exception handler for consistent error responses
   - Specific handlers for authentication errors
   - Detailed error messages for debugging

## Testing with cURL

### 1. Signup
```bash
curl -X POST http://localhost:8081/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

### 3. Access Protected Endpoint
```bash
curl -X GET http://localhost:8081/api/study-logs \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"
```

### 4. Refresh Token
```bash
curl -X POST http://localhost:8081/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN_HERE"
  }'
```

## Error Codes

| Error Code | Description | HTTP Status |
|------------|-------------|-------------|
| BAD_CREDENTIALS | Invalid username or password | 401 |
| INVALID_TOKEN | JWT token is invalid or malformed | 401 |
| TOKEN_EXPIRED | JWT token has expired | 401 |
| UNAUTHORIZED | Authentication required | 401 |
| DUPLICATE_RESOURCE | Username or email already exists | 409 |
| VALIDATION_ERROR | Input validation failed | 400 |

## Sample Users (Development)

The database is seeded with sample users (password: `password123`):

| Username | Email | Role |
|----------|-------|------|
| Admin User | admin@example.com | ADMIN |
| Normal User | user@example.com | USER |
| Manager User | manager@example.com | MANAGER |

## Best Practices

1. **Token Storage (Frontend)**
   - Store access token in memory (React state, Vue store)
   - Store refresh token in httpOnly cookie or secure storage
   - Never store tokens in localStorage for sensitive apps

2. **Token Refresh Strategy**
   - Implement automatic token refresh before expiration
   - Use refresh token to get new access token
   - Handle token expiration gracefully

3. **Security Headers**
   - Include CORS configuration for production
   - Use HTTPS in production
   - Set appropriate security headers

4. **Password Policy**
   - Minimum 6 characters (can be increased)
   - Consider adding complexity requirements
   - Implement password strength meter in frontend

## Troubleshooting

### 401 Unauthorized Error
- Check if token is included in Authorization header
- Verify token format: `Bearer <token>`
- Check if token has expired
- Verify JWT secret matches configuration

### 403 Forbidden Error
- User is authenticated but lacks required role
- Add role-based authorization if needed

### Token Validation Fails
- Ensure JWT secret is consistent across restarts
- Check token expiration time
- Verify token signature

## Future Enhancements

1. **Email Verification**
   - Send verification email on signup
   - Verify email before enabling account

2. **Password Reset**
   - Forgot password functionality
   - Password reset tokens

3. **OAuth2 Integration**
   - Google, GitHub login
   - Social authentication

4. **Rate Limiting**
   - Prevent brute force attacks
   - Limit login attempts

5. **Audit Logging**
   - Log authentication events
   - Track user activity

6. **Role-Based Access Control (RBAC)**
   - Fine-grained permissions
   - Method-level security with `@PreAuthorize`

## Support

For issues or questions:
1. Check application logs
2. Verify database connection
3. Check JWT configuration
4. Review error messages in API response

## License

This implementation is part of the Spring Study Diary project.

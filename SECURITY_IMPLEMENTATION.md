# Spring Security Implementation Guide

## Overview

This document describes the Spring Security implementation for the my-spring-study-diary project, including user registration, authentication, and JWT-based authorization.

## Implementation Summary

### 1. Dependencies Added (build.gradle)

- `spring-boot-starter-security` - Core Spring Security
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson` - JWT token generation and validation
- `spring-boot-starter-validation` - Input validation
- `spring-security-test` - Security testing support

### 2. Entity Classes

#### User Entity (`entity/User.java`)
- Fields: id, username, email, password, enabled, createdAt, updatedAt, roles
- Uses Lombok annotations for boilerplate code
- Implements helper methods for role management

#### Role Entity (`entity/Role.java`)
- Fields: id, name, description
- Implements proper equals() and hashCode() based on role name

### 3. Repository Classes

#### UserRepository (`repository/UserRepository.java`)
- JDBC-based implementation using JdbcTemplate
- Methods:
  - `findByUsername(String username)` - Find user by username
  - `findByEmail(String email)` - Find user by email
  - `findById(Long id)` - Find user by ID
  - `save(User user)` - Insert or update user
  - `existsByUsername(String username)` - Check username existence
  - `existsByEmail(String email)` - Check email existence
  - Handles user-role relationships

#### RoleRepository (`repository/RoleRepository.java`)
- JDBC-based role management
- Methods for finding, saving, and managing roles

### 4. Security Components

#### CustomUserDetails (`security/CustomUserDetails.java`)
- Implements Spring Security's UserDetails interface
- Wraps User entity and provides authentication information
- Maps user roles to GrantedAuthority

#### CustomUserDetailsService (`security/CustomUserDetailsService.java`)
- Implements UserDetailsService
- Loads user by username for authentication
- Used by Spring Security during login

#### JwtTokenProvider (`security/JwtTokenProvider.java`)
- Generates JWT tokens with configurable expiration
- Validates JWT tokens
- Extracts username from tokens
- Uses HMAC-SHA algorithm with secret key

#### JwtAuthenticationFilter (`security/JwtAuthenticationFilter.java`)
- OncePerRequestFilter that processes JWT tokens
- Extracts token from Authorization header
- Sets authentication in SecurityContext

#### SecurityConfig (`config/SecurityConfig.java`)
- Main security configuration
- Configures:
  - Password encoder (BCrypt)
  - Authentication provider (DAO-based)
  - Security filter chain
  - JWT filter registration
  - Public/protected endpoints
  - Stateless session management

### 5. Service Layer

#### UserService (`service/UserService.java`)
- Business logic for user operations
- Methods:
  - `signup(SignupRequest)` - Register new user
    - Validates username/email uniqueness
    - Encrypts password
    - Assigns default ROLE_USER
    - Generates JWT token
  - `login(LoginRequest)` - Authenticate user
    - Validates credentials via AuthenticationManager
    - Generates JWT token
  - `getCurrentUser()` - Get current authenticated user

### 6. DTOs

#### Request DTOs
- `SignupRequest` - username, email, password with validation
- `LoginRequest` - username, password with validation

#### Response DTOs
- `AuthResponse` - token, username, email, roles, message, tokenType

### 7. Controller

#### AuthController (`controller/AuthController.java`)
- REST endpoints:
  - `POST /api/auth/signup` - User registration
  - `POST /api/auth/login` - User login
  - `GET /api/auth/me` - Get current user info

### 8. Database Schema

Created `schema-security.sql` with:
- `users` table - User information
- `roles` table - Available roles
- `user_roles` table - Many-to-many relationship
- Default roles (ROLE_USER, ROLE_ADMIN)

### 9. Configuration

#### application.yml additions:
```yaml
app:
  jwt:
    secret: mySecretKeyForJWTTokenGenerationAndValidationWithAtLeast256BitsLength
    expiration-ms: 86400000 # 24 hours
```

Added Spring Security logging:
```yaml
logging:
  level:
    org.springframework.security: DEBUG
```

### 10. Exception Handling

Updated `GlobalExceptionHandler` to handle:
- `BadCredentialsException` - Invalid login credentials
- `UsernameNotFoundException` - User not found
- Returns appropriate HTTP status codes and error messages

## Database Setup

### 1. Run the schema script:
```sql
-- Execute the schema-security.sql file
source src/main/resources/schema-security.sql;
```

### 2. Verify tables created:
```sql
SHOW TABLES;
-- Should show: users, roles, user_roles
```

### 3. Verify default roles:
```sql
SELECT * FROM roles;
-- Should show ROLE_USER and ROLE_ADMIN
```

## Testing the Implementation

### 1. Register a new user:
```bash
curl -X POST http://localhost:8081/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

Expected Response:
```json
{
  "status": "success",
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "username": "testuser",
    "email": "test@example.com",
    "roles": ["ROLE_USER"],
    "message": "User registered successfully",
    "tokenType": "Bearer"
  }
}
```

### 2. Login:
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

### 3. Access protected endpoint:
```bash
curl -X GET http://localhost:8081/api/auth/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. Access public endpoint (no token needed):
```bash
curl -X GET http://localhost:8081/api/study-logs
```

## Security Configuration Details

### Public Endpoints:
- `/api/auth/**` - All authentication endpoints (signup, login)
- `/api/study-logs/**` - Study log endpoints (currently public)

### Protected Endpoints:
- All other endpoints require valid JWT token

### Session Management:
- Stateless (SessionCreationPolicy.STATELESS)
- No server-side session storage
- JWT tokens contain all necessary authentication information

### Password Encoding:
- BCrypt with default strength (10 rounds)
- Passwords are never stored in plain text

## Important Notes

### Security Best Practices:
1. **JWT Secret**: Change the JWT secret in production to a strong, random value
2. **HTTPS**: Always use HTTPS in production to prevent token interception
3. **Token Expiration**: Tokens expire after 24 hours (configurable)
4. **CSRF**: Disabled for stateless JWT authentication
5. **CORS**: Currently disabled, configure for production if needed

### Token Format:
- Header: `Authorization: Bearer <token>`
- Token includes: username, issued time, expiration time
- Signed with HMAC-SHA algorithm

### Error Responses:
- 401 Unauthorized - Invalid credentials or expired token
- 409 Conflict - Username or email already exists
- 400 Bad Request - Validation errors
- 500 Internal Server Error - Server errors

## File Structure

```
src/main/java/com/study/myspringstudydiary/
├── config/
│   └── SecurityConfig.java
├── controller/
│   └── AuthController.java
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java
│   │   └── SignupRequest.java
│   └── response/
│       └── AuthResponse.java
├── entity/
│   ├── User.java
│   └── Role.java
├── repository/
│   ├── UserRepository.java
│   └── RoleRepository.java
├── security/
│   ├── CustomUserDetails.java
│   ├── CustomUserDetailsService.java
│   ├── JwtAuthenticationFilter.java
│   └── JwtTokenProvider.java
├── service/
│   └── UserService.java
└── global/
    └── exception/
        └── GlobalExceptionHandler.java

src/main/resources/
├── schema-security.sql
└── application.yml
```

## Next Steps

1. **Run the SQL schema** to create necessary tables
2. **Build the project** to download dependencies
3. **Start the application** and test the endpoints
4. **Create an admin user** if needed
5. **Integrate authentication** with existing study log features
6. **Add role-based authorization** to study log endpoints if needed

## Future Enhancements

- Refresh token mechanism
- Email verification
- Password reset functionality
- OAuth2 integration (Google, GitHub)
- Role-based method security (@PreAuthorize)
- Account lockout after failed attempts
- Remember-me functionality

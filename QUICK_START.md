# Quick Start Guide - JWT Authentication

## Setup Instructions

### 1. Database Setup

Ensure MySQL is running and create the database:

```bash
mysql -u root -p
```

```sql
CREATE DATABASE IF NOT EXISTS diary_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

The application will automatically create tables and insert sample data when it starts.

### 2. Build the Application

```bash
./gradlew clean build
```

### 3. Run the Application

**Development mode (port 8081):**
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

**Or with default profile (port 8080):**
```bash
./gradlew bootRun
```

The application should start successfully and you'll see:
```
Started MySpringStudyDiaryApplication in X.XXX seconds
Tomcat started on port 8081 (http)
```

## Testing the Authentication System

### Test Users (Pre-configured)

The database includes 3 test users (password: `password123`):

| Email | Role | Username |
|-------|------|----------|
| admin@example.com | ADMIN | Admin User |
| user@example.com | USER | Normal User |
| manager@example.com | MANAGER | Manager User |

### 1. Login with Existing User

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user@example.com",
    "password": "password123"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 1800,
    "username": "Normal User",
    "email": "user@example.com"
  }
}
```

Save the `accessToken` for the next steps.

### 2. Access Protected Endpoint

```bash
curl http://localhost:8081/api/study-logs \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"
```

**Expected Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "Spring Boot 시작하기",
      ...
    }
  ]
}
```

### 3. Try Without Token (Should Fail)

```bash
curl http://localhost:8081/api/study-logs
```

**Expected Response (401 Unauthorized):**
```json
{
  "success": false,
  "errorCode": "UNAUTHORIZED",
  "errorMessage": "Authentication is required to access this resource"
}
```

### 4. Register New User

```bash
curl -X POST http://localhost:8081/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "securepass123"
  }'
```

**Expected Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "userId": 4,
    "username": "johndoe",
    "email": "john@example.com",
    "message": "User registered successfully"
  }
}
```

### 5. Login with New User

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "securepass123"
  }'
```

### 6. Refresh Access Token

When your access token expires (after 30 minutes), use the refresh token:

```bash
curl -X POST http://localhost:8081/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN_HERE"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "NEW_ACCESS_TOKEN",
    "refreshToken": "NEW_REFRESH_TOKEN",
    "tokenType": "Bearer",
    "expiresIn": 1800
  }
}
```

### 7. Logout

```bash
curl -X POST http://localhost:8081/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN_HERE"
  }'
```

## Common Issues and Solutions

### Issue: "Table 'diary_db.users' doesn't exist"

**Solution:** The database schema needs to be initialized.

Check if `spring.sql.init.mode` is set to `always` in `application.yml`:
```yaml
spring:
  sql:
    init:
      mode: always
```

Or manually run the schema:
```bash
mysql -u root -p diary_db < src/main/resources/db/schema.sql
```

### Issue: "401 Unauthorized" on Login

**Causes:**
1. Incorrect username/password
2. User not found in database
3. User account disabled

**Check:** Verify user exists:
```sql
SELECT * FROM users WHERE email = 'user@example.com';
```

### Issue: "Invalid JWT signature"

**Causes:**
1. JWT secret changed between requests
2. Token was generated with different secret

**Solution:** Restart the application to ensure consistent JWT secret.

### Issue: "Token has expired"

**Cause:** Access token expired (30 min limit)

**Solution:** Use refresh token to get new access token (see step 6 above).

## Useful MySQL Commands

### View All Users
```sql
SELECT id, email, username, role, enabled FROM users;
```

### View Refresh Tokens
```sql
SELECT rt.*, u.username
FROM refresh_tokens rt
JOIN users u ON rt.user_id = u.id;
```

### Delete User
```sql
DELETE FROM users WHERE email = 'test@example.com';
```

### Update User Role
```sql
UPDATE users SET role = 'ROLE_ADMIN' WHERE email = 'user@example.com';
```

## Application Configuration

### JWT Settings (application.yml)

```yaml
jwt:
  secret: ${JWT_SECRET:default-secret-key}
  access-token-validity: 1800  # 30 minutes in seconds
  refresh-token-validity: 604800  # 7 days in seconds
```

### Production Deployment

For production, set a secure JWT secret via environment variable:

```bash
export JWT_SECRET="your-very-long-secure-secret-key-at-least-64-characters"
./gradlew bootRun
```

## Public vs Protected Endpoints

### Public (No Auth Required)
- `POST /api/auth/login`
- `POST /api/auth/signup`
- `POST /api/auth/refresh`
- `GET /actuator/health`
- `GET /h2-console/**` (dev only)

### Protected (Auth Required)
- `GET /api/study-logs`
- `POST /api/study-logs`
- `PUT /api/study-logs/{id}`
- `DELETE /api/study-logs/{id}`
- All other endpoints

## Next Steps

1. Read [JWT_AUTHENTICATION_GUIDE.md](JWT_AUTHENTICATION_GUIDE.md) for detailed documentation
2. Review [JWT_IMPLEMENTATION_SUMMARY.md](JWT_IMPLEMENTATION_SUMMARY.md) for implementation details
3. Integrate frontend application with the API
4. Add role-based authorization if needed
5. Implement additional features (email verification, password reset, etc.)

## Support

If you encounter issues:
1. Check application logs in console
2. Verify database connection
3. Ensure MySQL is running
4. Check JWT configuration in application.yml
5. Review error messages in API responses

## Testing Tools

- **cURL** - Command line (shown above)
- **Postman** - GUI-based API testing
- **HTTPie** - Modern cURL alternative
- **Insomnia** - REST API client

Happy coding!

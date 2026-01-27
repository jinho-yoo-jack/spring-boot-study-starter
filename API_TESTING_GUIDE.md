# API Testing Guide - Spring Security Implementation

## Quick Reference

### Base URL
```
http://localhost:8081
```

## Authentication Endpoints

### 1. User Registration

**Endpoint:** `POST /api/auth/signup`

**Request:**
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
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huZG9lIiwiaWF0IjoxNzA2...",
    "username": "johndoe",
    "email": "john@example.com",
    "roles": ["ROLE_USER"],
    "message": "User registered successfully",
    "tokenType": "Bearer"
  },
  "error": null
}
```

**Validation Errors (400 Bad Request):**
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "username: Username must be between 3 and 50 characters"
  }
}
```

**Duplicate User (409 Conflict):**
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "DUPLICATE_RESOURCE",
    "message": "Username already exists: johndoe"
  }
}
```

### 2. User Login

**Endpoint:** `POST /api/auth/login`

**Request:**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "securepass123"
  }'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huZG9lIiwiaWF0IjoxNzA2...",
    "username": "johndoe",
    "email": "john@example.com",
    "roles": ["ROLE_USER"],
    "message": "Login successful",
    "tokenType": "Bearer"
  },
  "error": null
}
```

**Invalid Credentials (401 Unauthorized):**
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "AUTHENTICATION_FAILED",
    "message": "Invalid username or password"
  }
}
```

### 3. Get Current User

**Endpoint:** `GET /api/auth/me`

**Request:**
```bash
curl -X GET http://localhost:8081/api/auth/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "enabled": true,
    "createdAt": "2026-01-27T12:00:00",
    "updatedAt": "2026-01-27T12:00:00",
    "roles": [
      {
        "id": 1,
        "name": "ROLE_USER",
        "description": "Default user role"
      }
    ]
  },
  "error": null
}
```

**No Token or Invalid Token (401 Unauthorized):**
```
No response body - filtered by Spring Security
```

## Complete Testing Workflow

### Step 1: Register a New User
```bash
# Register
RESPONSE=$(curl -s -X POST http://localhost:8081/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }')

# Extract token (using jq)
TOKEN=$(echo $RESPONSE | jq -r '.data.token')
echo "Token: $TOKEN"
```

### Step 2: Login (Alternative to Signup)
```bash
# Login
RESPONSE=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }')

# Extract token
TOKEN=$(echo $RESPONSE | jq -r '.data.token')
echo "Token: $TOKEN"
```

### Step 3: Access Protected Endpoint
```bash
# Get current user
curl -X GET http://localhost:8081/api/auth/me \
  -H "Authorization: Bearer $TOKEN"
```

### Step 4: Access Public Endpoint (No Token Required)
```bash
# Get study logs (currently public)
curl -X GET http://localhost:8081/api/study-logs
```

## Testing Different Scenarios

### 1. Test Validation Errors

**Short Username:**
```bash
curl -X POST http://localhost:8081/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "ab",
    "email": "test@example.com",
    "password": "password123"
  }'
# Expected: 400 Bad Request - username must be between 3 and 50 characters
```

**Invalid Email:**
```bash
curl -X POST http://localhost:8081/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "invalid-email",
    "password": "password123"
  }'
# Expected: 400 Bad Request - Email should be valid
```

**Short Password:**
```bash
curl -X POST http://localhost:8081/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "12345"
  }'
# Expected: 400 Bad Request - password must be between 6 and 100 characters
```

### 2. Test Duplicate Prevention

**Register Same Username Twice:**
```bash
# First registration - should succeed
curl -X POST http://localhost:8081/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "duplicate",
    "email": "first@example.com",
    "password": "password123"
  }'

# Second registration - should fail
curl -X POST http://localhost:8081/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "duplicate",
    "email": "second@example.com",
    "password": "password123"
  }'
# Expected: 409 Conflict - Username already exists
```

### 3. Test Invalid Credentials

**Wrong Password:**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "wrongpassword"
  }'
# Expected: 401 Unauthorized - Invalid username or password
```

**Non-existent User:**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "nonexistent",
    "password": "password123"
  }'
# Expected: 401 Unauthorized - Invalid username or password
```

### 4. Test Token Expiration

Tokens expire after 24 hours (86400000 ms). To test expiration, you can:
1. Manually change the expiration time in `application.yml` to a shorter duration (e.g., 60000 ms = 1 minute)
2. Restart the application
3. Login and get a token
4. Wait for the token to expire
5. Try to access `/api/auth/me` with the expired token

## Postman Collection

### Import these requests into Postman:

1. **Signup**
   - Method: POST
   - URL: `{{base_url}}/api/auth/signup`
   - Body (JSON):
     ```json
     {
       "username": "{{username}}",
       "email": "{{email}}",
       "password": "{{password}}"
     }
     ```
   - Tests (extract token):
     ```javascript
     if (pm.response.code === 201) {
         var jsonData = pm.response.json();
         pm.environment.set("jwt_token", jsonData.data.token);
     }
     ```

2. **Login**
   - Method: POST
   - URL: `{{base_url}}/api/auth/login`
   - Body (JSON):
     ```json
     {
       "username": "{{username}}",
       "password": "{{password}}"
     }
     ```
   - Tests (extract token):
     ```javascript
     if (pm.response.code === 200) {
         var jsonData = pm.response.json();
         pm.environment.set("jwt_token", jsonData.data.token);
     }
     ```

3. **Get Current User**
   - Method: GET
   - URL: `{{base_url}}/api/auth/me`
   - Headers:
     - Key: `Authorization`
     - Value: `Bearer {{jwt_token}}`

### Postman Environment Variables:
```json
{
  "base_url": "http://localhost:8081",
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123",
  "jwt_token": ""
}
```

## Database Queries for Verification

### Check registered users:
```sql
SELECT id, username, email, enabled, created_at
FROM users;
```

### Check user roles:
```sql
SELECT u.username, r.name as role
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id;
```

### Verify password encryption:
```sql
SELECT username, password
FROM users
LIMIT 1;
-- Password should start with $2a$ (BCrypt)
```

## Troubleshooting

### Issue: 401 Unauthorized on protected endpoints
- **Cause:** Missing or invalid JWT token
- **Solution:**
  1. Ensure you're including the `Authorization` header
  2. Token format must be: `Bearer <token>`
  3. Token must not be expired
  4. User must exist in database

### Issue: 409 Conflict on signup
- **Cause:** Username or email already exists
- **Solution:** Use a different username/email or delete the existing user from database

### Issue: 400 Bad Request on signup/login
- **Cause:** Validation errors
- **Solution:** Check the error message for specific validation requirements

### Issue: 500 Internal Server Error
- **Cause:** Various (database connection, missing configuration, etc.)
- **Solution:**
  1. Check application logs
  2. Verify database connection
  3. Ensure all required tables exist
  4. Check JWT secret is configured in application.yml

## Security Notes

1. **JWT Secret:** The secret key in `application.yml` should be changed in production
2. **HTTPS:** Always use HTTPS in production to prevent token interception
3. **Token Storage:** Store tokens securely on the client side (not in localStorage for sensitive apps)
4. **Token Expiration:** Current expiration is 24 hours - adjust based on your security requirements
5. **Password Policy:** Minimum 6 characters - consider stronger requirements for production

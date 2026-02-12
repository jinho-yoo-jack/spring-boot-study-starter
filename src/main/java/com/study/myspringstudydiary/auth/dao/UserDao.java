package com.study.myspringstudydiary.auth.dao;

import com.study.myspringstudydiary.auth.entity.User;
import com.study.myspringstudydiary.auth.entity.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Optional;

/**
 * User DAO using JdbcTemplate
 * Handles database operations for User entity
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    /**
     * RowMapper: Converts each row of ResultSet to User object
     */
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        String roleStr = rs.getString("role");
        UserRole role = UserRole.USER; // default

        if (roleStr != null) {
            // Remove ROLE_ prefix if present
            if (roleStr.startsWith("ROLE_")) {
                roleStr = roleStr.substring(5);
            }
            try {
                role = UserRole.valueOf(roleStr);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid role value: {}, using default USER", roleStr);
            }
        }

        return User.builder()
                .id(rs.getLong("id"))
                .email(rs.getString("email"))
                .password(rs.getString("password"))
                .username(rs.getString("username"))
                .role(role)
                .enabled(rs.getBoolean("enabled"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                .build();
    };

    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, email);
            log.debug("Found user by email: {}", email);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            log.debug("No user found with email: {}", email);
            return Optional.empty();
        }
    }

    /**
     * Find user by username
     */
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, username);
            log.debug("Found user by username: {}", username);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            log.debug("No user found with username: {}", username);
            return Optional.empty();
        }
    }

    /**
     * Find user by ID
     */
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, id);
            log.debug("Found user by ID: {}", id);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            log.debug("No user found with ID: {}", id);
            return Optional.empty();
        }
    }

    /**
     * Check if email exists
     */
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    /**
     * Check if username exists
     */
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
    }

    /**
     * Create new user
     */
    public User save(User user) {
        String sql = "INSERT INTO users (email, password, username, role, enabled) VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getUsername());

            // Convert UserRole enum to String for database
            UserRole role = user.getRole() != null ? user.getRole() : UserRole.USER;
            ps.setString(4, "ROLE_" + role.name());

            ps.setBoolean(5, user.isEnabled());
            return ps;
        }, keyHolder);

        Long generatedId = keyHolder.getKey().longValue();
        log.info("Created new user with ID: {}", generatedId);

        return findById(generatedId).orElseThrow(
                () -> new RuntimeException("Failed to retrieve created user")
        );
    }

    /**
     * Update user
     */
    public int update(User user) {
        String sql = "UPDATE users SET email = ?, password = ?, username = ?, role = ?, enabled = ? WHERE id = ?";

        // Convert UserRole enum to String for database
        String roleStr = user.getRole() != null ? "ROLE_" + user.getRole().name() : "ROLE_USER";

        int rowsAffected = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getPassword(),
                user.getUsername(),
                roleStr,
                user.isEnabled(),
                user.getId()
        );

        log.debug("Updated user with ID: {}, rows affected: {}", user.getId(), rowsAffected);
        return rowsAffected;
    }

    /**
     * Delete user by ID
     */
    public int deleteById(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        log.debug("Deleted user with ID: {}, rows affected: {}", id, rowsAffected);
        return rowsAffected;
    }

    /**
     * Save refresh token
     */
    public void saveRefreshToken(Long userId, String token, Timestamp expiresAt) {
        // First, delete any existing refresh tokens for this user
        String deleteSql = "DELETE FROM refresh_tokens WHERE user_id = ?";
        jdbcTemplate.update(deleteSql, userId);

        // Insert new refresh token
        String insertSql = "INSERT INTO refresh_tokens (user_id, token, expires_at) VALUES (?, ?, ?)";
        jdbcTemplate.update(insertSql, userId, token, expiresAt);
        log.debug("Saved refresh token for user ID: {}", userId);
    }

    /**
     * Find user by refresh token
     */
    public Optional<User> findByRefreshToken(String refreshToken) {
        String sql = """
                SELECT u.* FROM users u
                INNER JOIN refresh_tokens rt ON u.id = rt.user_id
                WHERE rt.token = ? AND rt.expires_at > NOW()
                """;

        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, refreshToken);
            log.debug("Found user by refresh token");
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            log.debug("No user found with valid refresh token");
            return Optional.empty();
        }
    }

    /**
     * Delete refresh token
     */
    public void deleteRefreshToken(String refreshToken) {
        String sql = "DELETE FROM refresh_tokens WHERE token = ?";
        jdbcTemplate.update(sql, refreshToken);
        log.debug("Deleted refresh token");
    }

    /**
     * Delete all expired refresh tokens
     */
    public int deleteExpiredRefreshTokens() {
        String sql = "DELETE FROM refresh_tokens WHERE expires_at <= NOW()";
        int rowsAffected = jdbcTemplate.update(sql);
        log.debug("Deleted {} expired refresh tokens", rowsAffected);
        return rowsAffected;
    }
}

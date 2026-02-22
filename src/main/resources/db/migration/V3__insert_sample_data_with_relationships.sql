-- ============================================
-- V3: Insert Sample Data with Relationships
-- ============================================
-- Description: Insert sample users and study logs with proper relationships
-- Author: Spring Boot Study Diary
-- Date: 2024
-- ============================================

-- Insert sample users (if not exists)
INSERT INTO users (username, email, password, role, enabled, created_at, updated_at)
SELECT * FROM (
    SELECT 'testuser1' as username,
           'test1@example.com' as email,
           '$2a$10$8H0OT8wgtALJkig6fmypi.Y7jzI5Y7W9PGgJeE9MtDHJGmLnCcLhy' as password, -- password: password123
           'USER' as role,
           true as enabled,
           NOW() as created_at,
           NOW() as updated_at
) AS tmp
WHERE NOT EXISTS (
    SELECT username FROM users WHERE username = 'testuser1'
);

INSERT INTO users (username, email, password, role, enabled, created_at, updated_at)
SELECT * FROM (
    SELECT 'testuser2' as username,
           'test2@example.com' as email,
           '$2a$10$8H0OT8wgtALJkig6fmypi.Y7jzI5Y7W9PGgJeE9MtDHJGmLnCcLhy' as password, -- password: password123
           'USER' as role,
           true as enabled,
           NOW() as created_at,
           NOW() as updated_at
) AS tmp
WHERE NOT EXISTS (
    SELECT username FROM users WHERE username = 'testuser2'
);

INSERT INTO users (username, email, password, role, enabled, created_at, updated_at)
SELECT * FROM (
    SELECT 'admin' as username,
           'admin@example.com' as email,
           '$2a$10$8H0OT8wgtALJkig6fmypi.Y7jzI5Y7W9PGgJeE9MtDHJGmLnCcLhy' as password, -- password: password123
           'ADMIN' as role,
           true as enabled,
           NOW() as created_at,
           NOW() as updated_at
) AS tmp
WHERE NOT EXISTS (
    SELECT username FROM users WHERE username = 'admin'
);

-- Get user IDs for reference
SET @user1_id = (SELECT id FROM users WHERE username = 'testuser1');
SET @user2_id = (SELECT id FROM users WHERE username = 'testuser2');
SET @admin_id = (SELECT id FROM users WHERE username = 'admin');

-- Insert sample study logs for testuser1
INSERT INTO study_logs (user_id, title, content, category, understanding, study_time, study_date, created_at, updated_at)
VALUES
    (@user1_id, 'Spring Data JPA 기초', 'Entity 매핑과 Repository 인터페이스 학습', 'JPA', 'GOOD', 120, CURDATE(), NOW(), NOW()),
    (@user1_id, 'JPA 연관관계 매핑', '@OneToMany, @ManyToOne 관계 설정 방법 학습', 'JPA', 'NORMAL', 180, CURDATE(), NOW(), NOW()),
    (@user1_id, 'Spring Security 인증', 'JWT 토큰 기반 인증 구현', 'SPRING', 'GOOD', 150, DATE_SUB(CURDATE(), INTERVAL 1 DAY), NOW(), NOW()),
    (@user1_id, 'MySQL 인덱스 최적화', '쿼리 성능 개선을 위한 인덱스 설계', 'DATABASE', 'VERY_GOOD', 90, DATE_SUB(CURDATE(), INTERVAL 2 DAY), NOW(), NOW());

-- Insert sample study logs for testuser2
INSERT INTO study_logs (user_id, title, content, category, understanding, study_time, study_date, created_at, updated_at)
VALUES
    (@user2_id, 'Java Stream API', '함수형 프로그래밍과 Stream 활용법', 'JAVA', 'GOOD', 100, CURDATE(), NOW(), NOW()),
    (@user2_id, 'Spring Boot 시작하기', '프로젝트 설정과 기본 구조 이해', 'SPRING', 'VERY_GOOD', 60, CURDATE(), NOW(), NOW()),
    (@user2_id, 'Git 브랜치 전략', 'Git Flow와 GitHub Flow 비교', 'GIT', 'NORMAL', 45, DATE_SUB(CURDATE(), INTERVAL 1 DAY), NOW(), NOW());

-- Insert sample study logs for admin
INSERT INTO study_logs (user_id, title, content, category, understanding, study_time, study_date, created_at, updated_at)
VALUES
    (@admin_id, '시스템 아키텍처 설계', '마이크로서비스 아키텍처 패턴 연구', 'CS', 'VERY_GOOD', 240, CURDATE(), NOW(), NOW()),
    (@admin_id, '알고리즘 문제 풀이', '동적 프로그래밍 문제 10개 해결', 'ALGORITHM', 'GOOD', 180, DATE_SUB(CURDATE(), INTERVAL 1 DAY), NOW(), NOW());

-- ============================================
-- Verification Queries:
-- ============================================
-- SELECT u.username, COUNT(sl.id) as study_count
-- FROM users u
-- LEFT JOIN study_logs sl ON u.id = sl.user_id
-- GROUP BY u.id, u.username;
-- ============================================
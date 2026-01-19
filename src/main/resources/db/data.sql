-- 테스트용 사용자 데이터
INSERT INTO users (user_name, email, created_at, updated_at)
VALUES
('john_doe', 'john@example.com', NOW(), NOW()),
('jane_smith', 'jane@example.com', NOW(), NOW()),
('bob_wilson', 'bob@example.com', NOW(), NOW());

-- 테스트용 일기 데이터
INSERT INTO diary_entries (title, content, user_id, created_at, updated_at)
VALUES
('첫 번째 일기', '오늘은 Spring Boot 프로젝트를 시작했다. 새로운 기술을 배우는 것은 언제나 즐겁다.', 1, NOW(), NOW()),
('프로젝트 진행 상황', 'JPA와 MySQL 연동이 성공적으로 완료되었다. 이제 REST API를 만들어야 한다.', 1, NOW(), NOW()),
('학습 일지', '오늘은 Layered Architecture에 대해 깊이 있게 공부했다. 각 계층의 역할이 명확해지니 이해가 쉬워졌다.', 2, NOW(), NOW()),
('개발 회고', '이번 프로젝트를 통해 Spring Boot의 편리함을 다시 한번 느꼈다. 특히 자동 설정 기능이 인상적이다.', 2, NOW(), NOW()),
('기술 블로그', 'Docker를 이용한 MySQL 설정이 생각보다 간단했다. 컨테이너화의 장점을 체감했다.', 3, NOW(), NOW());
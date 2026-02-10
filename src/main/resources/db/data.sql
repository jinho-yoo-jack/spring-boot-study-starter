-- 테스트용 일기 데이터
-- 초기 데이터 삽입 (테스트용)
INSERT INTO study_logs (title, content, category, understanding, study_time, study_date)
VALUES ('Spring Boot 시작하기', 'Spring Boot 프로젝트 생성과 기본 설정을 학습했습니다.', 'SPRING', 'HIGH', 120, CURDATE()),
       ('Java Stream API', 'Stream API를 활용한 함수형 프로그래밍을 학습했습니다.', 'JAVA', 'MEDIUM', 90,
        DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
       ('MySQL 인덱스 최적화', '데이터베이스 인덱스 설계와 최적화 방법을 학습했습니다.', 'DATABASE', 'LOW', 60, DATE_SUB(CURDATE(), INTERVAL 2 DAY));


-- 초기 데이터 삽입 (테스트용)
INSERT INTO study_logs (title, content, category, understanding, study_time, study_date)
VALUES ('Spring Boot 시작하기', 'Spring Boot 프로젝트 생성과 기본 설정을 학습했습니다.', 'SPRING', 'VERY_GOOD', 120, CURDATE()),
       ('Java Stream API', 'Stream API를 활용한 함수형 프로그래밍을 학습했습니다.', 'JAVA', 'NORMAL', 90,
        DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
       ('MySQL 인덱스 최적화', '데이터베이스 인덱스 설계와 최적화 방법을 학습했습니다.', 'DATABASE', 'BAD', 60, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
       ('REST API 설계', 'RESTful API 설계 원칙과 best practice를 학습했습니다.', 'WEB', 'GOOD', 150,
        DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
       ('Spring Security 기초', '인증과 인가의 개념을 이해하고 Spring Security를 학습했습니다.', 'SPRING', 'NORMAL', 180,
        DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
       ('JPA 연관관계 매핑', 'OneToMany, ManyToOne 등 연관관계 매핑을 학습했습니다.', 'SPRING', 'GOOD', 200,
        DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
       ('Java 동시성 프로그래밍', 'Thread와 동시성 제어 방법을 학습했습니다.', 'JAVA', 'VERY_BAD', 240, DATE_SUB(CURDATE(), INTERVAL 6 DAY)),
       ('Docker 컨테이너화', 'Spring Boot 애플리케이션을 Docker로 컨테이너화하는 방법을 학습했습니다.', 'ETC', 'VERY_GOOD', 100,
        DATE_SUB(CURDATE(), INTERVAL 7 DAY));
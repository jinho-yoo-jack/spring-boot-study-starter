# My Spring Study Diary

Spring Boot를 이용한 간단한 일기 관리 REST API 프로젝트입니다. Layered Architecture와 CRUD 작업을 학습하기 위한 프로젝트입니다.

## 기술 스택

- **Language**: Java 17+
- **Framework**: Spring Boot 3.x
- **Database**: MySQL 8.0.28 (Production), H2 (Test)
- **ORM**: Spring Data JPA
- **Build Tool**: Gradle
- **Container**: Docker & Docker Compose

## 프로젝트 구조

```
src/main/java/com/study/myspringstudydiary/
├── entity/          # JPA 엔티티
├── repository/      # Spring Data JPA 리포지토리
├── service/         # 비즈니스 로직
├── controller/      # REST API 엔드포인트
├── dto/             # 데이터 전송 객체
├── exception/       # 커스텀 예외 및 글로벌 핸들러
├── config/          # 설정 클래스
└── util/            # 유틸리티 클래스
```

## 시작하기

### 1. 환경 변수 설정

`.env.example` 파일을 복사하여 `.env` 파일을 생성하고 실제 값으로 수정합니다:

```bash
cp .env.example .env
# .env 파일을 열어서 실제 비밀번호 등을 설정
```

### 2. 데이터베이스 설정

Docker Compose를 사용하여 MySQL을 실행합니다:

```bash
docker-compose up -d
```

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

### 4. 테스트 실행

```bash
./gradlew test
```

## API 엔드포인트

### User API

- `POST /api/users` - 사용자 생성
- `GET /api/users/{id}` - ID로 사용자 조회
- `GET /api/users/username/{userName}` - 사용자명으로 사용자 조회
- `GET /api/users` - 모든 사용자 조회
- `PUT /api/users/{id}` - 사용자 정보 수정
- `DELETE /api/users/{id}` - 사용자 삭제

### Diary API

- `POST /api/diaries` - 일기 생성
- `GET /api/diaries/{id}` - ID로 일기 조회
- `GET /api/diaries/user/{userId}` - 사용자별 일기 조회 (페이징)
- `GET /api/diaries/user/{userId}/date-range` - 날짜 범위로 일기 조회
- `GET /api/diaries/search?keyword={keyword}` - 키워드로 일기 검색
- `PUT /api/diaries/{id}` - 일기 수정
- `DELETE /api/diaries/{id}` - 일기 삭제
- `GET /api/diaries/user/{userId}/count` - 사용자별 일기 개수

## API 요청/응답 예시

### 사용자 생성
```json
POST /api/users
{
    "user_name": "john_doe",
    "email": "john@example.com"
}
```

### 일기 생성
```json
POST /api/diaries
{
    "title": "오늘의 일기",
    "content": "오늘은 Spring Boot를 공부했다.",
    "user_id": 1
}
```

## 프로파일 설정

- **개발 환경**: `application.yml` (MySQL 사용)
- **테스트 환경**: `application-test.yml` (H2 인메모리 DB 사용)

테스트 프로파일로 실행:
```bash
./gradlew bootRun --args='--spring.profiles.active=test'
```

## H2 Console 접속 (테스트 환경)

테스트 프로파일로 실행 후:
- URL: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:testdb
- Username: sa
- Password: (비워두기)

## 주요 기능

- RESTful API 설계
- JPA를 사용한 데이터 영속성 관리
- 계층형 아키텍처 (Controller → Service → Repository → Entity)
- 전역 예외 처리 (@RestControllerAdvice)
- Bean Validation을 사용한 요청 데이터 검증
- JPA Auditing을 통한 생성/수정 시간 자동 관리
- 페이징 및 정렬 지원
- Docker Compose를 통한 데이터베이스 관리
- 환경 변수를 통한 민감한 정보 관리 (.env 파일)

## 개발 가이드

자세한 개발 가이드는 [.CLAUDE.md](./.CLAUDE.md) 파일을 참조하세요.
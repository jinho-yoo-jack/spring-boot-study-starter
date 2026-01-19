# Project: Spring Study Diary

## 🎯 Objective
Build a complete CRUD application using Spring Boot to learn layered architecture and best practices

## 📅 Timeline
- **Start Date**: 2026-01-15
- **Due Date**: 2026-02-28
- **Status**: 🟡 In Progress

## ✅ Success Criteria
- [x] Implement full CRUD operations for User and Diary entities
- [x] Apply layered architecture (Controller → Service → Repository → Entity)
- [x] Set up proper exception handling
- [ ] Integrate Redis for caching
- [ ] Implement security (authentication & authorization)
- [ ] Achieve 80% test coverage
- [ ] Complete documentation

## 📋 Current Tasks
- [x] Project setup and configuration
- [x] Create entity classes (User, Diary)
- [x] Implement repository layer
- [x] Implement service layer
- [x] Create REST controllers
- [x] Set up global exception handler
- [x] Configure Docker Compose for MySQL
- [ ] Integrate Redis caching
- [ ] Add Spring Security
- [ ] Write unit tests
- [ ] Write integration tests
- [ ] Create API documentation

## 🏗️ Architecture

### Tech Stack
- **Framework**: Spring Boot 3.x
- **Language**: Java 17
- **Database**: MySQL 8.0.28
- **Cache**: Redis 7
- **Build Tool**: Gradle
- **Container**: Docker

### Package Structure
```
com.study.myspringstudydiary/
├── controller/    # REST API endpoints
├── service/       # Business logic
├── repository/    # Data access layer
├── entity/        # JPA entities
├── dto/           # Data transfer objects
├── exception/     # Custom exceptions
├── config/        # Configuration classes
└── util/          # Utility classes
```

## 📚 Key Documents
- [[redis-spring-boot-config|Redis Spring Boot Configuration]]
- [[../../3-Resources/Guides/redis-password-setup-guide|Redis Security Setup]]
- [[api-documentation|API Documentation]] (TODO)
- [[testing-strategy|Testing Strategy]] (TODO)

## 🔗 Related Resources
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Project Repository](https://github.com/yourusername/my-spring-study-diary)

## 📊 Progress Metrics
- **Code Coverage**: 25%
- **API Endpoints**: 12/12 completed
- **Test Cases**: 1/30 written
- **Documentation**: 40% complete

## 📝 Meeting Notes & Decisions

### 2026-01-19: Architecture Decisions
- Decided to use Layered Architecture over Hexagonal
- Chose Redis for caching instead of Hazelcast
- Will use JWT for authentication

### 2026-01-15: Initial Setup
- Created project structure
- Set up development environment
- Configured Docker Compose

## 🐛 Issues & Blockers
- None currently

## 💡 Lessons Learned
1. **JPA Auditing**: Using `@CreatedDate` and `@LastModifiedDate` simplifies timestamp management
2. **Docker Compose**: Environment variables in `.env` file improve security
3. **Exception Handling**: `@RestControllerAdvice` provides clean global error handling

## 🎯 Next Steps
1. Complete Redis integration
2. Implement user authentication
3. Add comprehensive test suite
4. Deploy to cloud platform

---

## 📂 Project Files

### Configuration Files
- [[docker-compose.yml]] - Container orchestration
- [[application.yml]] - Spring Boot configuration
- [[build.gradle]] - Build configuration

### Source Code
- Entities: `User.java`, `Diary.java`, `BaseEntity.java`
- Services: `UserService.java`, `DiaryService.java`
- Controllers: `UserController.java`, `DiaryController.java`

---

*Last updated: 2026-01-19*
*Project version: 0.3.0*
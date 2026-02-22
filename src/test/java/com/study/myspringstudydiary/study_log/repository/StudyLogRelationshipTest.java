package com.study.myspringstudydiary.study_log.repository;

import com.study.myspringstudydiary.auth.entity.User;
import com.study.myspringstudydiary.auth.entity.UserRole;
import com.study.myspringstudydiary.auth.repository.UserRepository;
import com.study.myspringstudydiary.study_log.entity.Category;
import com.study.myspringstudydiary.study_log.entity.StudyLog;
import com.study.myspringstudydiary.study_log.entity.Understanding;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * StudyLog와 User 간의 연관관계 테스트
 * - N:1 단방향 관계 테스트
 * - 1:N 양방향 관계 테스트
 * - Cascade 동작 테스트
 * - Orphan Removal 테스트
 */
@DataJpaTest
@ActiveProfiles("test")
class StudyLogRelationshipTest {

    @Autowired
    private StudyLogRepository studyLogRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private User testUser;
    private StudyLog testStudyLog;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .role(UserRole.USER)
                .enabled(true)
                .build();
        testUser = userRepository.save(testUser);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("N:1 단방향 관계 - StudyLog에서 User 참조")
    void testManyToOneRelationship() {
        // Given: StudyLog 생성 및 User 설정
        StudyLog studyLog = StudyLog.builder()
                .user(testUser)
                .title("JPA 연관관계 학습")
                .content("N:1 관계 매핑 테스트")
                .category(Category.JPA)
                .understanding(Understanding.GOOD)
                .studyTime(120)
                .studyDate(LocalDate.now())
                .build();

        // When: StudyLog 저장
        StudyLog savedStudyLog = studyLogRepository.save(studyLog);
        entityManager.flush();
        entityManager.clear();

        // Then: 저장된 StudyLog에서 User 조회 가능
        StudyLog foundStudyLog = studyLogRepository.findById(savedStudyLog.getId()).orElseThrow();
        assertThat(foundStudyLog.getUser()).isNotNull();
        assertThat(foundStudyLog.getUser().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("1:N 양방향 관계 - User에서 StudyLog 목록 접근")
    void testOneToManyBidirectionalRelationship() {
        // Given: User 조회
        User user = userRepository.findById(testUser.getId()).orElseThrow();

        // StudyLog 생성 및 양방향 관계 설정
        StudyLog studyLog1 = StudyLog.builder()
                .title("Spring Boot 기초")
                .content("스프링 부트 시작하기")
                .category(Category.SPRING)
                .understanding(Understanding.GOOD)
                .studyTime(60)
                .studyDate(LocalDate.now())
                .build();

        StudyLog studyLog2 = StudyLog.builder()
                .title("Spring Security")
                .content("인증과 인가")
                .category(Category.SPRING)
                .understanding(Understanding.NORMAL)
                .studyTime(90)
                .studyDate(LocalDate.now())
                .build();

        // 양방향 관계 설정 (편의 메서드 사용)
        user.addStudyLog(studyLog1);
        user.addStudyLog(studyLog2);

        // When: 저장
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        // Then: User를 통해 StudyLog 목록 접근 가능
        User foundUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(foundUser.getStudyLogs()).hasSize(2);
        assertThat(foundUser.getStudyLogs())
                .extracting(StudyLog::getTitle)
                .containsExactlyInAnyOrder("Spring Boot 기초", "Spring Security");
    }

    @Test
    @DisplayName("User별 StudyLog 조회")
    void testFindByUserId() {
        // Given: 여러 StudyLog 생성
        StudyLog studyLog1 = createStudyLog(testUser, "JPA 기초", Category.JPA);
        StudyLog studyLog2 = createStudyLog(testUser, "Spring Boot", Category.SPRING);
        StudyLog studyLog3 = createStudyLog(testUser, "MySQL", Category.DATABASE);

        studyLogRepository.saveAll(List.of(studyLog1, studyLog2, studyLog3));
        entityManager.flush();
        entityManager.clear();

        // When: User ID로 StudyLog 조회
        List<StudyLog> userStudyLogs = studyLogRepository.findByUserId(testUser.getId());

        // Then: 해당 User의 StudyLog만 조회됨
        assertThat(userStudyLogs).hasSize(3);
        assertThat(userStudyLogs)
                .extracting(StudyLog::getTitle)
                .containsExactlyInAnyOrder("JPA 기초", "Spring Boot", "MySQL");
    }

    @Test
    @DisplayName("User와 Category로 StudyLog 조회")
    void testFindByUserAndCategory() {
        // Given: 다양한 카테고리의 StudyLog 생성
        StudyLog jpaLog1 = createStudyLog(testUser, "JPA 기초", Category.JPA);
        StudyLog jpaLog2 = createStudyLog(testUser, "JPA 심화", Category.JPA);
        StudyLog springLog = createStudyLog(testUser, "Spring Boot", Category.SPRING);

        studyLogRepository.saveAll(List.of(jpaLog1, jpaLog2, springLog));
        entityManager.flush();
        entityManager.clear();

        // When: User와 Category로 조회
        List<StudyLog> jpaStudyLogs = studyLogRepository.findByUserAndCategory(testUser, Category.JPA);

        // Then: 해당 Category의 StudyLog만 조회됨
        assertThat(jpaStudyLogs).hasSize(2);
        assertThat(jpaStudyLogs)
                .extracting(StudyLog::getTitle)
                .containsExactlyInAnyOrder("JPA 기초", "JPA 심화");
    }

    @Test
    @DisplayName("Cascade.ALL - User 저장 시 StudyLog도 함께 저장")
    void testCascadePersist() {
        // Given: 새로운 User 생성
        User newUser = User.builder()
                .username("cascadetest")
                .email("cascade@example.com")
                .password("password")
                .role(UserRole.USER)
                .enabled(true)
                .build();

        // StudyLog 생성 및 User에 추가
        StudyLog studyLog = StudyLog.builder()
                .title("Cascade 테스트")
                .content("CascadeType.ALL 테스트")
                .category(Category.JPA)
                .understanding(Understanding.GOOD)
                .studyTime(30)
                .studyDate(LocalDate.now())
                .build();

        newUser.addStudyLog(studyLog);

        // When: User만 저장 (Cascade로 StudyLog도 자동 저장)
        User savedUser = userRepository.save(newUser);
        entityManager.flush();
        entityManager.clear();

        // Then: StudyLog도 함께 저장됨
        List<StudyLog> studyLogs = studyLogRepository.findByUserId(savedUser.getId());
        assertThat(studyLogs).hasSize(1);
        assertThat(studyLogs.get(0).getTitle()).isEqualTo("Cascade 테스트");
    }

    @Test
    @DisplayName("Orphan Removal - User에서 StudyLog 제거 시 DB에서도 삭제")
    void testOrphanRemoval() {
        // Given: User와 StudyLog 설정
        User user = userRepository.findById(testUser.getId()).orElseThrow();

        StudyLog studyLog = StudyLog.builder()
                .title("Orphan Removal 테스트")
                .content("고아 객체 제거 테스트")
                .category(Category.JPA)
                .understanding(Understanding.NORMAL)
                .studyTime(45)
                .studyDate(LocalDate.now())
                .build();

        user.addStudyLog(studyLog);
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        // Re-fetch the user and studyLog with generated IDs
        user = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(user.getStudyLogs()).hasSize(1);
        studyLog = user.getStudyLogs().get(0);
        Long studyLogId = studyLog.getId();
        assertThat(studyLogId).isNotNull();
        assertThat(studyLogRepository.existsById(studyLogId)).isTrue();

        // When: User에서 StudyLog 제거
        user.removeStudyLog(studyLog);
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        // Then: DB에서도 StudyLog가 삭제됨
        assertThat(studyLogRepository.existsById(studyLogId)).isFalse();
    }

    @Test
    @DisplayName("N+1 문제 해결 - Fetch Join 사용")
    void testFetchJoin() {
        // Given: 여러 StudyLog 생성
        for (int i = 1; i <= 5; i++) {
            StudyLog studyLog = createStudyLog(testUser, "Study " + i, Category.JPA);
            studyLogRepository.save(studyLog);
        }
        entityManager.flush();
        entityManager.clear();

        // When: Fetch Join으로 조회
        List<StudyLog> studyLogs = studyLogRepository.findAllWithUser();

        // Then: User 정보가 함께 로드됨 (추가 쿼리 없음)
        assertThat(studyLogs).isNotEmpty();
        studyLogs.forEach(studyLog -> {
            assertThat(studyLog.getUser()).isNotNull();
            assertThat(studyLog.getUser().getUsername()).isNotBlank();
        });
    }

    @Test
    @DisplayName("날짜 범위와 User로 StudyLog 조회")
    void testFindByUserIdAndDateRange() {
        // Given: 다양한 날짜의 StudyLog 생성
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate twoDaysAgo = today.minusDays(2);

        StudyLog todayLog = createStudyLogWithDate(testUser, "오늘 학습", today);
        StudyLog yesterdayLog = createStudyLogWithDate(testUser, "어제 학습", yesterday);
        StudyLog oldLog = createStudyLogWithDate(testUser, "이틀 전 학습", twoDaysAgo);

        studyLogRepository.saveAll(List.of(todayLog, yesterdayLog, oldLog));
        entityManager.flush();
        entityManager.clear();

        // When: 어제부터 오늘까지의 StudyLog 조회
        List<StudyLog> recentLogs = studyLogRepository.findByUserIdAndStudyDateBetween(
                testUser.getId(), yesterday, today);

        // Then: 해당 기간의 StudyLog만 조회됨
        assertThat(recentLogs).hasSize(2);
        assertThat(recentLogs)
                .extracting(StudyLog::getTitle)
                .containsExactlyInAnyOrder("오늘 학습", "어제 학습");
    }

    @Test
    @DisplayName("User별 StudyLog 개수 카운트")
    void testCountByUserId() {
        // Given: 여러 StudyLog 생성
        for (int i = 1; i <= 3; i++) {
            StudyLog studyLog = createStudyLog(testUser, "Study " + i, Category.JPA);
            studyLogRepository.save(studyLog);
        }
        entityManager.flush();
        entityManager.clear();

        // When: User별 카운트
        long count = studyLogRepository.countByUserId(testUser.getId());

        // Then: 정확한 개수 반환
        assertThat(count).isEqualTo(3);
    }

    // Helper methods
    private StudyLog createStudyLog(User user, String title, Category category) {
        return StudyLog.builder()
                .user(user)
                .title(title)
                .content("Content for " + title)
                .category(category)
                .understanding(Understanding.GOOD)
                .studyTime(60)
                .studyDate(LocalDate.now())
                .build();
    }

    private StudyLog createStudyLogWithDate(User user, String title, LocalDate date) {
        return StudyLog.builder()
                .user(user)
                .title(title)
                .content("Content for " + title)
                .category(Category.JPA)
                .understanding(Understanding.GOOD)
                .studyTime(60)
                .studyDate(date)
                .build();
    }
}
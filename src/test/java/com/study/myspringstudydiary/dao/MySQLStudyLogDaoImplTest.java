package com.study.myspringstudydiary.dao;

import com.study.myspringstudydiary.entity.Category;
import com.study.myspringstudydiary.entity.StudyLog;
import com.study.myspringstudydiary.entity.Understanding;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest  // JDBC 레이어 테스트, H2 인메모리 DB 사용
@ActiveProfiles("test")
@Import(MySQLStudyLogDaoImpl.class)  // DAO 구현체 Import
@DisplayName("StudyLog DAO 테스트")
class MySQLStudyLogDaoImplTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MySQLStudyLogDaoImpl studyLogDao;

    @BeforeEach
    void setUp() {
        studyLogDao = new MySQLStudyLogDaoImpl(jdbcTemplate);

        // 테스트용 users 테이블에 사용자 추가 (ID를 명시적으로 지정)
        jdbcTemplate.update(
            "INSERT INTO users (id, username, email, password) VALUES (?, ?, ?, ?)",
            1L, "testuser", "test@example.com", "password123"
        );
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM study_logs");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    @DisplayName("학습 기록 저장 및 조회")
    void saveAndFindById() {
        // Given
        StudyLog studyLog = new StudyLog();
        studyLog.setUserId(1L);
        studyLog.setTitle("JPA 학습");
        studyLog.setContent("Entity 매핑 완료");
        studyLog.setCategory(Category.DATABASE);
        studyLog.setUnderstanding(Understanding.NORMAL);
        studyLog.setStudyTime(90);
        studyLog.setStudyDate(LocalDate.now());

        // When
        StudyLog saved = studyLogDao.save(studyLog);
        Optional<StudyLog> found = studyLogDao.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("JPA 학습");
        assertThat(found.get().getUserId()).isEqualTo(1L);
        assertThat(found.get().getCategory()).isEqualTo(Category.DATABASE);
        assertThat(found.get().getUnderstanding()).isEqualTo(Understanding.NORMAL);
    }

    @Test
    @DisplayName("사용자별 학습 기록 조회")
    void findByUserId() {
        // Given
        createStudyLogsForUser(1L, 3);

        // When
        List<StudyLog> userLogs = studyLogDao.findByUserId(1L);

        // Then
        assertThat(userLogs).hasSize(3);
        assertThat(userLogs).allSatisfy(log ->
            assertThat(log.getUserId()).isEqualTo(1L)
        );
    }

    @Test
    @DisplayName("학습 기록 수정")
    void update() {
        // Given
        StudyLog studyLog = createStudyLog(1L, "Original Title");
        Long id = studyLog.getId();

        // When
        studyLog.setTitle("Updated Title");
        studyLog.setStudyTime(200);
        studyLog.setUnderstanding(Understanding.VERY_GOOD);
        StudyLog updated = studyLogDao.update(studyLog);

        // Then
        Optional<StudyLog> found = studyLogDao.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Updated Title");
        assertThat(found.get().getStudyTime()).isEqualTo(200);
        assertThat(found.get().getUnderstanding()).isEqualTo(Understanding.VERY_GOOD);
    }

    @Test
    @DisplayName("학습 기록 삭제")
    void deleteById() {
        // Given
        StudyLog studyLog = createStudyLog(1L, "To Delete");
        Long id = studyLog.getId();

        // When
        studyLogDao.deleteById(id);

        // Then
        Optional<StudyLog> found = studyLogDao.findById(id);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("전체 개수 조회")
    void count() {
        // Given
        createStudyLogsForUser(1L, 5);

        // When
        long count = studyLogDao.count();

        // Then
        assertThat(count).isEqualTo(5);
    }

    @Test
    @DisplayName("모든 학습 기록 조회")
    void findAll() {
        // Given
        createStudyLogsForUser(1L, 3);

        // When
        List<StudyLog> all = studyLogDao.findAll();

        // Then
        assertThat(all).hasSize(3);
    }

    @Test
    @DisplayName("카테고리별 조회")
    void findByCategory() {
        // Given
        createStudyLogWithCategory(1L, "Spring Study", Category.SPRING);
        createStudyLogWithCategory(1L, "Java Study", Category.JAVA);
        createStudyLogWithCategory(1L, "Database Study", Category.DATABASE);

        // When
        List<StudyLog> springLogs = studyLogDao.findAll().stream()
            .filter(log -> log.getCategory() == Category.SPRING)
            .toList();

        // Then
        assertThat(springLogs).hasSize(1);
        assertThat(springLogs.get(0).getTitle()).isEqualTo("Spring Study");
    }

    // Helper methods
    private StudyLog createStudyLog(Long userId, String title) {
        StudyLog studyLog = new StudyLog();
        studyLog.setUserId(userId);
        studyLog.setTitle(title);
        studyLog.setContent("Content for " + title);
        studyLog.setCategory(Category.SPRING);
        studyLog.setUnderstanding(Understanding.NORMAL);
        studyLog.setStudyTime(60);
        studyLog.setStudyDate(LocalDate.now());

        return studyLogDao.save(studyLog);
    }

    private StudyLog createStudyLogWithCategory(Long userId, String title, Category category) {
        StudyLog studyLog = new StudyLog();
        studyLog.setUserId(userId);
        studyLog.setTitle(title);
        studyLog.setContent("Content for " + title);
        studyLog.setCategory(category);
        studyLog.setUnderstanding(Understanding.NORMAL);
        studyLog.setStudyTime(60);
        studyLog.setStudyDate(LocalDate.now());

        return studyLogDao.save(studyLog);
    }

    private void createStudyLogsForUser(Long userId, int count) {
        for (int i = 0; i < count; i++) {
            createStudyLog(userId, "Study " + i + " for user " + userId);
        }
    }
}
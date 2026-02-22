package com.study.myspringstudydiary.study_log.repository;

import com.study.myspringstudydiary.auth.entity.User;
import com.study.myspringstudydiary.study_log.entity.Category;
import com.study.myspringstudydiary.study_log.entity.StudyLog;
import com.study.myspringstudydiary.study_log.entity.Understanding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudyLogRepository extends JpaRepository<StudyLog, Long> {

    // ========== User 관련 Query Methods ==========
    // user_id로 조회
    List<StudyLog> findByUserId(Long userId);

    // User 엔티티로 조회
    List<StudyLog> findByUser(User user);

    // User와 Category로 조회
    List<StudyLog> findByUserAndCategory(User user, Category category);

    // User ID와 날짜 범위로 조회
    List<StudyLog> findByUserIdAndStudyDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    // User별 카운트
    long countByUserId(Long userId);

    // ========== 기존 Query Methods ==========
    // Query Method - 카테고리별 조회
    List<StudyLog> findByCategory(Category category);

    // 이해도별 조회
    List<StudyLog> findByUnderstanding(Understanding understanding);

    // 학습 시간 기준 조회
    List<StudyLog> findByStudyTimeGreaterThan(Integer minutes);

    // 날짜 범위 조회
    List<StudyLog> findByStudyDateBetween(LocalDate startDate, LocalDate endDate);

    // 여러 조건 조회
    List<StudyLog> findByCategoryAndUnderstandingAndStudyTimeGreaterThan(
        Category category,
        Understanding understanding,
        Integer minTime
    );

    // 제목 검색
    List<StudyLog> findByTitleContainingIgnoreCase(String keyword);

    // Top N 조회
    List<StudyLog> findTop10ByCategory(Category category);

    // JPQL - 카테고리별 조회
    @Query("SELECT s FROM StudyLog s WHERE s.category = :category")
    List<StudyLog> findByCategoryWithQuery(@Param("category") Category category);

    // Native Query - 카테고리별 조회
    @Query(
        value = "SELECT * FROM study_logs WHERE category = ?1",
        nativeQuery = true
    )
    List<StudyLog> findByCategoryNative(String category);

    // 날짜 범위 검색 (JPQL)
    @Query("SELECT s FROM StudyLog s WHERE s.studyDate BETWEEN :startDate AND :endDate")
    List<StudyLog> findByDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // 검색 조건 (동적 쿼리 대체) - User 필터링 추가
    @Query("SELECT s FROM StudyLog s WHERE " +
           "(:userId IS NULL OR s.user.id = :userId) AND " +
           "(:title IS NULL OR s.title LIKE %:title%) AND " +
           "(:category IS NULL OR s.category = :category) AND " +
           "(:startDate IS NULL OR s.studyDate >= :startDate) AND " +
           "(:endDate IS NULL OR s.studyDate <= :endDate)")
    List<StudyLog> searchWithConditions(
        @Param("userId") Long userId,
        @Param("title") String title,
        @Param("category") Category category,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // ID로 조회 with Fetch Join (N+1 방지)
    @Query("SELECT s FROM StudyLog s WHERE s.id = :id")
    Optional<StudyLog> findByIdOptimized(@Param("id") Long id);

    // ========== User 관련 JPQL Queries ==========
    // JPQL로 User 정보와 함께 조회 (Fetch Join)
    @Query("SELECT s FROM StudyLog s JOIN FETCH s.user WHERE s.id = :id")
    Optional<StudyLog> findByIdWithUser(@Param("id") Long id);

    // 특정 사용자의 모든 StudyLog 조회 (Fetch Join)
    @Query("SELECT s FROM StudyLog s JOIN FETCH s.user WHERE s.user.id = :userId")
    List<StudyLog> findByUserIdWithUser(@Param("userId") Long userId);

    // 모든 StudyLog를 User와 함께 조회 (N+1 문제 해결)
    @Query("SELECT s FROM StudyLog s JOIN FETCH s.user")
    List<StudyLog> findAllWithUser();

    // 벌크 업데이트
    @Modifying
    @Query("UPDATE StudyLog s SET s.studyTime = :hours WHERE s.id = :id")
    void updateStudyHours(@Param("id") Long id, @Param("hours") Integer hours);

    // 전문 검색 (MySQL Full-text search)
    @Query(
        value = "SELECT * FROM study_logs WHERE MATCH(title, content) AGAINST(?1 IN BOOLEAN MODE)",
        nativeQuery = true
    )
    List<StudyLog> searchFullText(String keyword);

    // 카운트 쿼리
    long countByCategory(Category category);

    // 카테고리별 학습 시간 통계
    @Query("SELECT s.category, SUM(s.studyTime) FROM StudyLog s GROUP BY s.category")
    List<Object[]> findStudyTimeByCategory();

    // 사용자별 카테고리 학습 시간 통계
    @Query("SELECT s.category, SUM(s.studyTime) FROM StudyLog s WHERE s.user.id = :userId GROUP BY s.category")
    List<Object[]> findStudyTimeByCategoryForUser(@Param("userId") Long userId);

    // 날짜별 학습 일지 조회
    List<StudyLog> findByStudyDate(LocalDate date);

    // 오늘의 학습 일지
    default List<StudyLog> findTodayStudyLogs() {
        return findByStudyDate(LocalDate.now());
    }
}
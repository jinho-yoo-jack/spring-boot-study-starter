package com.study.myspringstudydiary.study_log.repository;

import com.study.myspringstudydiary.study_log.entity.Category;
import com.study.myspringstudydiary.study_log.entity.StudyLog;
import com.study.myspringstudydiary.study_log.entity.Understanding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * StudyLog JPA Repository
 * Spring Data JPA를 사용한 학습 기록 데이터 접근 인터페이스
 */
@Repository
public interface StudyLogRepository extends JpaRepository<StudyLog, Long> {

    // ========== 기본 Query Method ==========

    /**
     * 카테고리로 학습 기록 조회
     */
    List<StudyLog> findByCategory(Category category);

    /**
     * 이해도로 학습 기록 조회
     */
    List<StudyLog> findByUnderstanding(Understanding understanding);

    /**
     * 학습 날짜 범위로 조회
     */
    List<StudyLog> findByStudyDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * 제목에 특정 키워드가 포함된 학습 기록 조회
     */
    List<StudyLog> findByTitleContaining(String keyword);

    /**
     * 카테고리와 이해도로 학습 기록 조회
     */
    List<StudyLog> findByCategoryAndUnderstanding(Category category, Understanding understanding);

    /**
     * 특정 학습 시간 이상의 기록 조회
     */
    List<StudyLog> findByStudyTimeGreaterThanEqual(Integer studyTime);

    /**
     * 최근 생성된 학습 기록 조회 (상위 N개)
     */
    List<StudyLog> findTop10ByOrderByCreatedAtDesc();

    /**
     * 특정 날짜의 학습 기록 조회
     */
    List<StudyLog> findByStudyDate(LocalDate studyDate);

    // ========== @Query를 사용한 커스텀 쿼리 ==========

    /**
     * 카테고리별 학습 시간 총합 조회
     */
    @Query("SELECT s.category, SUM(s.studyTime) FROM StudyLog s GROUP BY s.category")
    List<Object[]> findTotalStudyTimeByCategory();

    /**
     * 특정 기간 동안의 총 학습 시간 조회
     */
    @Query("SELECT SUM(s.studyTime) FROM StudyLog s WHERE s.studyDate BETWEEN :startDate AND :endDate")
    Integer getTotalStudyTimeBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 이해도별 학습 기록 개수 조회
     */
    @Query("SELECT s.understanding, COUNT(s) FROM StudyLog s GROUP BY s.understanding")
    List<Object[]> countByUnderstanding();

    /**
     * 최근 수정된 학습 기록 조회
     */
    @Query("SELECT s FROM StudyLog s WHERE s.updatedAt IS NOT NULL ORDER BY s.updatedAt DESC")
    List<StudyLog> findRecentlyUpdated();

    /**
     * 키워드 검색 (제목 또는 내용)
     */
    @Query("SELECT s FROM StudyLog s WHERE s.title LIKE %:keyword% OR s.content LIKE %:keyword%")
    List<StudyLog> searchByKeyword(@Param("keyword") String keyword);
}
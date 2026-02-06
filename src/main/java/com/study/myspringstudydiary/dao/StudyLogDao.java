package com.study.myspringstudydiary.dao;

import com.study.myspringstudydiary.common.Page;
import com.study.myspringstudydiary.entity.StudyLog;
import com.study.myspringstudydiary.entity.Category;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Study Log DAO Interface
 *
 * DAO (Data Access Object) Pattern:
 * - Pattern that abstracts database access logic
 * - Separates business logic and data access logic
 * - Interface remains unchanged even when database type changes
 * - Only implementation needs to be replaced (MySQL, PostgreSQL, MongoDB, etc.)
 */
public interface StudyLogDao {

    // ========== CREATE ==========

    /**
     * Create study log
     * @param studyLog Study log to save
     * @return Saved study log (with ID)
     */
    StudyLog save(StudyLog studyLog);

    // ========== READ ==========

    /**
     * Find study log by ID
     * @param id Study log ID to find
     * @return Study log (wrapped in Optional to prevent null)
     */
    Optional<StudyLog> findById(Long id);

    /**
     * Find all study logs
     * @return List of all study logs
     */
    List<StudyLog> findAll();

    /**
     * Find study logs by category (String version for backward compatibility)
     * @param category Category as string
     * @return List of study logs in the category
     */
    List<StudyLog> findByCategory(String category);

    /**
     * Find study logs by category
     * @param category Category enum
     * @return List of study logs in the category
     */
    List<StudyLog> findByCategory(Category category);

    /**
     * Find study logs by study date
     * @param date Study date
     * @return List of study logs for the date
     */
    List<StudyLog> findByStudyDate(LocalDate date);

    /**
     * Check if ID exists
     * @param id Study log ID to check
     * @return Existence
     */
    boolean existsById(Long id);

    /**
     * Get total count of study logs
     * @return Total count of study logs
     */
    long count();

    // ========== UPDATE ==========

    /**
     * Update study log
     * @param studyLog Study log to update
     * @return Updated study log
     */
    StudyLog update(StudyLog studyLog);

    // ========== DELETE ==========

    /**
     * Delete study log by ID
     * @param id Study log ID to delete
     * @return Success/failure of deletion
     */
    boolean deleteById(Long id);

    /**
     * Delete all study logs
     * WARNING: Use only for testing
     */
    void deleteAll();

    // ========== PAGING ==========

    /**
     * 전체 학습 일지를 페이징하여 조회
     * @param page 페이지 번호 (0-based)
     * @param size 페이지당 데이터 개수
     * @return 페이징된 결과
     */
    Page<StudyLog> findAllWithPaging(int page, int size);

    /**
     * 카테고리별 학습 일지를 페이징하여 조회
     * @param category 카테고리
     * @param page 페이지 번호 (0-based)
     * @param size 페이지당 데이터 개수
     * @return 페이징된 결과
     */
    Page<StudyLog> findByCategoryWithPaging(Category category, int page, int size);

    /**
     * 날짜별 학습 일지를 페이징하여 조회
     * @param date 조회할 날짜
     * @param page 페이지 번호 (0-based)
     * @param size 페이지당 데이터 개수
     * @return 페이징된 결과
     */
    Page<StudyLog> findByDateWithPaging(LocalDate date, int page, int size);

    /**
     * 검색 조건과 함께 페이징하여 조회
     * @param titleKeyword 제목 키워드 (null 가능)
     * @param category 카테고리 (null 가능)
     * @param startDate 시작 날짜 (null 가능)
     * @param endDate 종료 날짜 (null 가능)
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 페이징된 결과
     */
    Page<StudyLog> searchWithPaging(
            String titleKeyword,
            Category category,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size);
}
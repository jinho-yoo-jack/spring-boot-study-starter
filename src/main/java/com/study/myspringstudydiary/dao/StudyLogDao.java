package com.study.myspringstudydiary.dao;

import com.study.myspringstudydiary.entity.StudyLog;
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

    /**
     * Create study log
     * @param studyLog Study log to save
     * @return Saved study log (with ID)
     */
    StudyLog save(StudyLog studyLog);

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
     * Find study logs by category
     * @param category Category
     * @return List of study logs in the category
     */
    List<StudyLog> findByCategory(String category);

    /**
     * Update study log
     * @param studyLog Study log to update
     * @return Updated study log
     */
    StudyLog update(StudyLog studyLog);

    /**
     * Delete study log by ID
     * @param id Study log ID to delete
     * @return Success/failure of deletion
     */
    boolean deleteById(Long id);

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

    /**
     * Delete all study logs
     * WARNING: Use only for testing
     */
    void deleteAll();
}
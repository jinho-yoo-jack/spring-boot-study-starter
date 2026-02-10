package com.study.myspringstudydiary.dao;

import com.study.myspringstudydiary.entity.StudyLog;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * StudyLog DAO 인터페이스
 * <p>
 * DAO(Data Access Object) 패턴:
 * - 데이터베이스 접근 로직을 캡슐화
 * - 비즈니스 로직과 데이터 접근 로직을 분리
 * - 데이터베이스 종류에 독립적인 인터페이스 제공
 */
public interface StudyLogDao {

    // ========== CREATE ==========
    StudyLog save(StudyLog studyLog);

    // ========== READ ==========
    Optional<StudyLog> findById(Long id);

    List<StudyLog> findByStudyDate(LocalDate id);

    List<StudyLog> findAll();

    List<StudyLog> findByCategory(String category);

    // ========== UPDATE ==========
    StudyLog update(StudyLog studyLog);

    // ========== DELETE ==========
    boolean deleteById(Long id);

    boolean existsById(Long id);

    long count();

    void deleteAll();
}

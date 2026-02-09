package com.study.myspringstudydiary.repository;

import com.study.myspringstudydiary.dao.StudyLogDao;
import com.study.myspringstudydiary.entity.StudyLog;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * StudyLog Repository
 *
 * DAO를 호출하여 영속성을 처리하는 Repository 계층
 * Service와 DAO 사이의 추가적인 추상화 계층 역할
 */
@Repository
public class StudyLogRepository {

    private final StudyLogDao studyLogDao;

    public StudyLogRepository(StudyLogDao studyLogDao) {
        this.studyLogDao = studyLogDao;
    }

    // ========== CREATE ==========

    /**
     * 학습 일지 저장
     * @param studyLog 저장할 학습 일지
     * @return 저장된 학습 일지 (ID 포함)
     */
    public StudyLog save(StudyLog studyLog) {
        return studyLogDao.save(studyLog);
    }

    // ========== READ ==========

    /**
     * ID로 학습 일지 조회
     * @param id 조회할 학습 일지 ID
     * @return 학습 일지 (없으면 null)
     */
    public StudyLog findById(Long id) {
        return studyLogDao.findById(id).orElse(null);
    }

    /**
     * 모든 학습 일지 조회
     * @return 모든 학습 일지 리스트
     */
    public List<StudyLog> findAll() {
        return studyLogDao.findAll();
    }

    // ========== UPDATE ==========

    /**
     * 학습 일지 수정
     * @param studyLog 수정할 학습 일지
     * @return 수정된 학습 일지
     * @throws IllegalArgumentException 해당 ID의 학습 일지가 없는 경우
     * @throws RuntimeException 업데이트 실패 시
     */
    public StudyLog update(StudyLog studyLog) {
        if (!existsById(studyLog.getId())) {
            throw new IllegalArgumentException(
                "해당 학습 일지를 찾을 수 없습니다. (id: " + studyLog.getId() + ")");
        }

        StudyLog updatedStudyLog = studyLogDao.update(studyLog);
        if (updatedStudyLog == null) {
            throw new RuntimeException("업데이트 실패");
        }

        return updatedStudyLog;
    }

    // ========== DELETE ==========

    /**
     * ID로 학습 일지 삭제
     * @param id 삭제할 학습 일지 ID
     * @return 삭제 성공 여부
     */
    public boolean deleteById(Long id) {
        return studyLogDao.deleteById(id);
    }

    /**
     * ID 존재 여부 확인
     * @param id 확인할 학습 일지 ID
     * @return 존재 여부
     */
    public boolean existsById(Long id) {
        return studyLogDao.existsById(id);
    }

    /**
     * 학습 일지 총 개수 조회
     * @return 학습 일지 총 개수
     */
    public long count() {
        return studyLogDao.count();
    }

    /**
     * 모든 학습 일지 삭제
     * 주의: 테스트 용도로만 사용
     */
    public void deleteAll() {
        studyLogDao.deleteAll();
    }
}
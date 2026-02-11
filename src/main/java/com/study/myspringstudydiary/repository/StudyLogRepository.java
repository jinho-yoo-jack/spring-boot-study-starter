package com.study.myspringstudydiary.repository;

import com.study.myspringstudydiary.entity.StudyLog;
import com.study.myspringstudydiary.dao.StudyLogDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 학습 일지 저장소 - Repository 패턴
 *
 * 역할:
 * - Service와 DAO 사이의 중간 계층
 * - DAO를 통해 데이터 접근을 수행
 * - 복잡한 데이터 접근 로직을 캡슐화
 * - 여러 DAO를 조합한 복잡한 연산 처리 가능
 *
 * Repository 패턴의 장점:
 * - 데이터 접근 로직과 비즈니스 로직 분리
 * - 테스트 용이성 향상
 * - 데이터 소스 변경 시 유연한 대응
 */
@Repository
public class StudyLogRepository {

    private final StudyLogDao studyLogDao;

    /**
     * 생성자 주입을 통한 의존성 주입
     * Spring이 StudyLogDao 구현체를 자동으로 주입
     */
    public StudyLogRepository(StudyLogDao studyLogDao) {
        this.studyLogDao = studyLogDao;
    }

    /**
     * 학습 일지 저장
     * @param studyLog 저장할 학습 일지
     * @return 저장된 학습 일지 (ID 포함)
     */
    public StudyLog save(StudyLog studyLog) {
        return studyLogDao.save(studyLog);
    }

    /**
     * ID로 학습 일지 조회
     * @param id 조회할 학습 일지 ID
     * @return 학습 일지 (Optional로 감싸서 반환)
     */
    public Optional<StudyLog> findById(Long id) {
        return studyLogDao.findById(id);
    }

    /**
     * 모든 학습 일지 조회
     * @return 모든 학습 일지 리스트
     */
    public List<StudyLog> findAll() {
        return studyLogDao.findAll();
    }

    /**
     * 카테고리별 학습 일지 조회
     * @param category 카테고리 문자열
     * @return 해당 카테고리의 학습 일지 리스트
     */
    public List<StudyLog> findByCategory(String category) {
        return studyLogDao.findByCategory(category);
    }

    /**
     * 학습 일지 수정
     * @param studyLog 수정할 학습 일지
     * @return 수정된 학습 일지
     */
    public StudyLog update(StudyLog studyLog) {
        return studyLogDao.update(studyLog);
    }

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
     * 전체 학습 일지 개수 조회
     * @return 전체 학습 일지 개수
     */
    public long count() {
        return studyLogDao.count();
    }

    /**
     * 모든 학습 일지 삭제
     * 주의: 테스트용으로만 사용
     */
    public void deleteAll() {
        studyLogDao.deleteAll();
    }
}
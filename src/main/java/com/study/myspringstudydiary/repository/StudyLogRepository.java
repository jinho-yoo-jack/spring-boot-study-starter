package com.study.myspringstudydiary.repository;

import com.study.myspringstudydiary.entity.StudyLog;
import org.springframework.stereotype.Repository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 학습 일지 저장소
 *
 * @Repository 어노테이션 설명:
 * - 이 클래스를 Spring Bean으로 등록합니다
 * - 데이터 접근 계층임을 명시합니다
 * - 데이터 접근 관련 예외를 Spring의 DataAccessException으로 변환해줍니다
 *
 * 실제 프로젝트에서는 JPA, MyBatis 등을 사용하지만,
 * 이번 강의에서는 Map을 사용하여 데이터를 저장합니다.
 */
@Repository  // ⭐ Spring Bean으로 등록!
public class StudyLogRepository {

    // 데이터 저장소 (실제 DB 대신 Map 사용)
    private final Map<Long, StudyLog> database = new HashMap<>();

    // ID 자동 증가를 위한 시퀀스
    private final AtomicLong sequence = new AtomicLong(1);

    /**
     * 학습 일지 저장
     * @param studyLog 저장할 학습 일지
     * @return 저장된 학습 일지 (ID 포함)
     */
    public StudyLog save(StudyLog studyLog) {
        // ID가 없으면 새로운 ID 부여
        if (studyLog.getId() == null) {
            studyLog.setId(sequence.getAndIncrement());
        }

        // Map에 저장
        database.put(studyLog.getId(), studyLog);

        return studyLog;
    }

    /**
     * 모든 학습 일지 조회
     * @return 모든 학습 일지 리스트
     */
    public List<StudyLog> findAll() {
        // Map의 모든 값을 리스트로 변환하여 반환
        return new ArrayList<>(database.values());
    }

    /**
     * ID로 학습 일지 조회
     * @param id 조회할 학습 일지 ID
     * @return 학습 일지 (없으면 null)
     */
    public StudyLog findById(Long id) {
        // Map에서 ID로 조회
        return database.get(id);
    }

    /**
     * 학습 일지 수정 (Update)
     * Map은 같은 키로 put하면 덮어쓰므로 save와 동일하게 동작
     * 하지만 의미를 명확히 하기 위해 별도 메서드로 분리
     */
    public StudyLog update(StudyLog studyLog) {
        if (studyLog.getId() == null) {
            throw new IllegalArgumentException("수정할 학습 일지의 ID가 없습니다.");
        }
        if (!database.containsKey(studyLog.getId())) {
            throw new IllegalArgumentException(
                "해당 학습 일지를 찾을 수 없습니다. (id: " + studyLog.getId() + ")");
        }
        database.put(studyLog.getId(), studyLog);
        return studyLog;
    }

    // ========== DELETE ==========

    /**
     * ID로 학습 일지를 삭제합니다.
     *
     * @param id 삭제할 학습 일지 ID
     * @return 삭제 성공 여부 (true: 삭제됨, false: 해당 ID 없음)
     */
    public boolean deleteById(Long id) {
        // Map.remove()는 삭제된 값을 반환, 없으면 null 반환
        StudyLog removed = database.remove(id);
        return removed != null;
    }

    /**
     * ID에 해당하는 학습 일지가 존재하는지 확인합니다.
     *
     * @param id 확인할 학습 일지 ID
     * @return 존재 여부
     */
    public boolean existsById(Long id) {
        return database.containsKey(id);
    }

    /**
     * 저장된 전체 학습 일지 수를 반환합니다.
     *
     * @return 학습 일지 총 개수
     */
    public long count() {
        return database.size();
    }

    /**
     * 모든 학습 일지를 삭제합니다.
     * (테스트용)
     */
    public void deleteAll() {
        database.clear();
    }

    // ========== 생명주기 콜백 ==========

    @PostConstruct
    public void init() {
        System.out.println("========================================");
        System.out.println("📦 StudyLogRepository 초기화 완료!");
        System.out.println("   - 데이터 저장소(Map) 준비됨");
        System.out.println("   - ID 생성기 준비됨");
        System.out.println("========================================");
    }

    @PreDestroy
    public void cleanup() {
        System.out.println("========================================");
        System.out.println("🧹 StudyLogRepository 정리 중...");
        System.out.println("   - 저장된 데이터 수: " + database.size());
        System.out.println("   - 마지막 ID: " + (sequence.get() - 1));
        database.clear();  // 데이터 정리
        System.out.println("   - 데이터 정리 완료!");
        System.out.println("========================================");
    }
}
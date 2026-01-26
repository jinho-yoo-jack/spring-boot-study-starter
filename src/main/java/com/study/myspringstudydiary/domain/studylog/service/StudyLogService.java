package com.study.myspringstudydiary.domain.studylog.service;

import com.study.myspringstudydiary.domain.studylog.dto.request.StudyLogCreateRequest;
import com.study.myspringstudydiary.domain.studylog.dto.request.StudyLogUpdateRequest;
import com.study.myspringstudydiary.domain.studylog.dto.response.StudyLogResponse;
import com.study.myspringstudydiary.domain.studylog.entity.Category;
import com.study.myspringstudydiary.domain.studylog.entity.StudyLog;
import com.study.myspringstudydiary.domain.studylog.entity.Understanding;
import com.study.myspringstudydiary.domain.studylog.repository.StudyLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 학습 일지 서비스
 *
 * @Service 어노테이션 설명:
 * - 이 클래스를 Spring Bean으로 등록합니다
 * - 비즈니스 로직을 담당하는 서비스 계층임을 명시합니다
 * - @Component와 기능적으로 동일하지만, 역할을 명확히 표현합니다
 */
@Service  // ⭐ Spring Bean으로 등록!
public class StudyLogService {

    // ⭐ 의존성 주입: Repository를 주입받음
    private final StudyLogRepository studyLogRepository;

    /**
     * 생성자 주입 (Constructor Injection)
     *
     * Spring이 StudyLogRepository Bean을 찾아서 자동으로 주입해줍니다.
     * 생성자가 1개만 있으면 @Autowired 생략 가능!
     */
    public StudyLogService(StudyLogRepository studyLogRepository) {
        this.studyLogRepository = studyLogRepository;
    }

    /**
     * 학습 일지 생성
     * @param request 생성 요청 DTO
     * @return 생성된 학습 일지 응답 DTO
     */
    public StudyLogResponse createStudyLog(StudyLogCreateRequest request) {

        // 1. 요청 데이터 유효성 검증
        validateCreateRequest(request);

        // 2. DTO → Entity 변환
        StudyLog studyLog = new StudyLog(
            null,  // ID는 Repository에서 자동 생성
            request.getTitle(),
            request.getContent(),
            Category.valueOf(request.getCategory()),
            Understanding.valueOf(request.getUnderstanding()),
            request.getStudyTime(),
            request.getStudyDate() != null ? request.getStudyDate() : LocalDate.now()
        );

        // 3. 저장
        StudyLog savedStudyLog = studyLogRepository.save(studyLog);

        // 4. Entity → Response DTO 변환 후 반환
        return StudyLogResponse.from(savedStudyLog);
    }

    /**
     * 모든 학습 일지 조회
     * @return 모든 학습 일지 응답 DTO 리스트
     */
    public List<StudyLogResponse> getAllStudyLogs() {
        // 1. Repository에서 모든 학습 일지 조회
        List<StudyLog> studyLogs = studyLogRepository.findAll();

        // 2. Entity 리스트 → Response DTO 리스트 변환
        return studyLogs.stream()
                .map(StudyLogResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * ID로 학습 일지 조회
     * @param id 조회할 학습 일지 ID
     * @return 학습 일지 응답 DTO
     */
    public StudyLogResponse getStudyLogById(Long id) {
        // 1. Repository에서 ID로 조회
        StudyLog studyLog = studyLogRepository.findById(id);

        // 2. 존재하지 않으면 예외 처리
        if (studyLog == null) {
            throw new IllegalArgumentException("ID " + id + "에 해당하는 학습 일지를 찾을 수 없습니다.");
        }

        // 3. Entity → Response DTO 변환 후 반환
        return StudyLogResponse.from(studyLog);
    }

    /**
     * 학습 일지 수정
     *
     * @param id 수정할 학습 일지 ID
     * @param request 수정 요청 데이터
     * @return 수정된 학습 일지 응답
     */
    public StudyLogResponse updateStudyLog(Long id, StudyLogUpdateRequest request) {

        // 1. 기존 학습 일지 조회
        StudyLog studyLog = studyLogRepository.findById(id);
        if (studyLog == null) {
            throw new IllegalArgumentException(
                "해당 학습 일지를 찾을 수 없습니다. (id: " + id + ")");
        }

        // 2. 수정할 내용이 있는지 확인
        if (request.hasNoUpdates()) {
            throw new IllegalArgumentException("수정할 내용이 없습니다.");
        }

        // 3. 수정할 값들의 유효성 검증
        validateUpdateRequest(request);

        // 4. 카테고리와 이해도 변환 (null이 아닌 경우에만)
        Category category = null;
        if (request.getCategory() != null) {
            try {
                category = Category.valueOf(request.getCategory().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                    "유효하지 않은 카테고리입니다: " + request.getCategory());
            }
        }

        Understanding understanding = null;
        if (request.getUnderstanding() != null) {
            try {
                understanding = Understanding.valueOf(request.getUnderstanding().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                    "유효하지 않은 이해도입니다: " + request.getUnderstanding());
            }
        }

        // 5. Entity 업데이트 (null이 아닌 값만 반영)
        studyLog.update(
            request.getTitle(),
            request.getContent(),
            category,
            understanding,
            request.getStudyTime(),
            request.getStudyDate()
        );

        // 6. 저장 및 응답 반환
        StudyLog updatedStudyLog = studyLogRepository.update(studyLog);
        return StudyLogResponse.from(updatedStudyLog);
    }

    /**
     * 생성 요청 유효성 검증
     */
    private void validateCreateRequest(StudyLogCreateRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("학습 주제는 필수입니다.");
        }
        if (request.getTitle().length() > 100) {
            throw new IllegalArgumentException("학습 주제는 100자를 초과할 수 없습니다.");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("학습 내용은 필수입니다.");
        }
        if (request.getContent().length() > 1000) {
            throw new IllegalArgumentException("학습 내용은 1000자를 초과할 수 없습니다.");
        }
        if (request.getStudyTime() == null || request.getStudyTime() < 1) {
            throw new IllegalArgumentException("학습 시간은 1분 이상이어야 합니다.");
        }
    }

    /**
     * 수정 요청 유효성 검증
     * null이 아닌 값만 검증합니다.
     */
    private void validateUpdateRequest(StudyLogUpdateRequest request) {
        if (request.getTitle() != null) {
            if (request.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("학습 주제는 빈 값일 수 없습니다.");
            }
            if (request.getTitle().length() > 100) {
                throw new IllegalArgumentException("학습 주제는 100자를 초과할 수 없습니다.");
            }
        }

        if (request.getContent() != null) {
            if (request.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("학습 내용은 빈 값일 수 없습니다.");
            }
            if (request.getContent().length() > 1000) {
                throw new IllegalArgumentException("학습 내용은 1000자를 초과할 수 없습니다.");
            }
        }

        if (request.getStudyTime() != null && request.getStudyTime() < 1) {
            throw new IllegalArgumentException("학습 시간은 1분 이상이어야 합니다.");
        }

        if (request.getStudyDate() != null && request.getStudyDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("학습 날짜는 미래일 수 없습니다.");
        }
    }
}
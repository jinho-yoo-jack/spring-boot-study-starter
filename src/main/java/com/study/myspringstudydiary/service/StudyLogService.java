package com.study.myspringstudydiary.service;

import com.study.myspringstudydiary.dto.request.StudyLogCreateRequest;
import com.study.myspringstudydiary.dto.response.StudyLogResponse;
import com.study.myspringstudydiary.entity.Category;
import com.study.myspringstudydiary.entity.StudyLog;
import com.study.myspringstudydiary.entity.Understanding;
import com.study.myspringstudydiary.repository.StudyLogRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

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
     *  생성자 함수를 통해서 객체를 초기화 하고 생성한 후에,
     *  특정한 설정해야 하는 경우가 종종 있습니다.
     *  @PostConstruct
     *  Post' 이후에
     *  Constructor 호출된 이후에
     *  */
    @PostConstruct
    public void init() {
        System.out.println("StudyLogService init");
        System.out.println(studyLogRepository);
        System.out.println(studyLogRepository == null);
    }

    @PreDestroy
    public void destroy() {
        System.out.println("StudyLogService destroy");
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
}
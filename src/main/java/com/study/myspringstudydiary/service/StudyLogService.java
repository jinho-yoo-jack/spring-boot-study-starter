package com.study.myspringstudydiary.service;

import com.study.myspringstudydiary.dto.request.PageRequest;
import com.study.myspringstudydiary.dto.request.StudyLogCreateRequest;
import com.study.myspringstudydiary.dto.request.StudyLogUpdateRequest;
import com.study.myspringstudydiary.dto.response.PageResponse;
import com.study.myspringstudydiary.dto.response.StudyLogResponse;
import com.study.myspringstudydiary.dto.response.StudyLogDeleteResponse;
import com.study.myspringstudydiary.exception.StudyLogNotFoundException;
import com.study.myspringstudydiary.entity.Category;
import com.study.myspringstudydiary.entity.StudyLog;
import com.study.myspringstudydiary.entity.Understanding;
import com.study.myspringstudydiary.dao.StudyLogDao;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 학습 일지 서비스
 *
 * DIP(Dependency Inversion Principle) 적용:
 * - Service(고수준)가 구체적인 Repository(저수준)에 의존하지 않음
 * - StudyLogDao 인터페이스(추상화)에만 의존
 * - 구현체(MapStudyLogRepository, MySQLStudyLogDaoImpl 등)는 언제든 교체 가능
 *
 * @Service 어노테이션 설명:
 * - 이 클래스를 Spring Bean으로 등록합니다
 * - 비즈니스 로직을 담당하는 서비스 계층임을 명시합니다
 * - @Component와 기능적으로 동일하지만, 역할을 명확히 표현합니다
 */
@Service  // ⭐ Spring Bean으로 등록!
public class StudyLogService {

    // ⭐ DIP 준수: 인터페이스에만 의존
    private final StudyLogDao studyLogDao;

    /**
     * 생성자 주입 (Constructor Injection)
     *
     * Spring이 StudyLogDao 인터페이스의 구현체를 찾아서 자동으로 주입
     * 현재는 MapStudyLogRepository가 주입됨
     * 향후 MySQLStudyLogDaoImpl 등으로 쉽게 교체 가능
     */
    public StudyLogService(StudyLogDao studyLogDao) {
        this.studyLogDao = studyLogDao;
    }

    // ==================== CREATE ====================

    /**
     * 학습 일지 생성
     *
     * @param request 생성 요청 DTO
     * @return 생성된 학습 일지 응답 DTO
     */
    public StudyLogResponse createStudyLog(StudyLogCreateRequest request) {

        // 1. 요청 데이터 유효성 검증
        validateCreateRequest(request);

        // 2. DTO → Entity 변환
        StudyLog studyLog = new StudyLog(
                null,  // ID는 DB에서 자동 생성
                request.getTitle(),
                request.getContent(),
                Category.valueOf(request.getCategory()),
                Understanding.valueOf(request.getUnderstanding()),
                request.getStudyTime(),
                request.getStudyDate() != null ? request.getStudyDate() : LocalDate.now()
        );

        // 3. 저장 (DAO 사용 - DIP 준수)
        StudyLog savedStudyLog = studyLogDao.save(studyLog);

        // 4. Entity → Response DTO 변환 후 반환
        return StudyLogResponse.from(savedStudyLog);
    }

    // ==================== READ ====================

    /**
     * 모든 학습 일지 조회
     *
     * @return 모든 학습 일지 응답 DTO 리스트
     */
    public List<StudyLogResponse> getAllStudyLogs() {
        // 1. DAO에서 모든 학습 일지 조회
        List<StudyLog> studyLogs = studyLogDao.findAll();

        // 2. Entity 리스트 → Response DTO 리스트 변환
        return studyLogs.stream()
                .map(StudyLogResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * ID로 학습 일지 조회
     *
     * @param id 조회할 학습 일지 ID
     * @return 학습 일지 응답 DTO
     */
    public StudyLogResponse getStudyLogById(Long id) {
        // 1. DAO에서 ID로 조회 (Optional 반환)
        StudyLog studyLog = studyLogDao.findById(id)
                .orElseThrow(() -> new StudyLogNotFoundException(id));

        // 2. Entity → Response DTO 변환 후 반환
        return StudyLogResponse.from(studyLog);
    }

    /**
     * 날짜별 학습 일지 조회
     *
     * @param date 조회할 날짜
     * @return 해당 날짜의 학습 일지 응답 DTO 리스트
     */
    public List<StudyLogResponse> getStudyLogsByDate(LocalDate date) {
        List<StudyLog> studyLogs = studyLogDao.findByStudyDate(date);
        return studyLogs.stream()
                .map(StudyLogResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 카테고리별 학습 일지 조회
     *
     * @param categoryString 조회할 카테고리 문자열
     * @return 해당 카테고리의 학습 일지 응답 DTO 리스트
     */
    public List<StudyLogResponse> getStudyLogsByCategory(String categoryString) {
        // 1. 문자열을 Category Enum으로 변환
        Category category;
        try {
            category = Category.valueOf(categoryString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 카테고리입니다. 사용 가능한 카테고리: " +
                    Arrays.toString(Category.values()));
        }

        // 2. DAO에서 카테고리로 조회
        List<StudyLog> studyLogs = studyLogDao.findByCategory(category.toString());

        // 3. Entity 리스트 → Response DTO 리스트 변환
        return studyLogs.stream()
                .map(StudyLogResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 페이징 처리된 학습 일지 목록 조회
     * 주의: 현재는 DB가 페이징을 직접 지원하지 않으므로 메모리에서 처리
     * TODO: 실제 프로덕션에서는 DB 레벨 페이징 구현 필요
     */
    public PageResponse<StudyLogResponse> getStudyLogsWithPaging(PageRequest pageRequest) {
        // 1. 전체 데이터 조회
        List<StudyLog> allLogs = studyLogDao.findAll();

        // 2. 정렬 처리
        allLogs.sort((a, b) -> {
            int result = switch (pageRequest.getSortBy()) {
                case "title" -> a.getTitle().compareTo(b.getTitle());
                case "studyTime" -> a.getStudyTime().compareTo(b.getStudyTime());
                case "studyDate" -> a.getStudyDate().compareTo(b.getStudyDate());
                default -> a.getCreatedAt().compareTo(b.getCreatedAt());
            };
            return "ASC".equals(pageRequest.getSortDirection()) ? result : -result;
        });

        // 3. 페이징 처리
        long totalElements = allLogs.size();
        int start = pageRequest.getOffset();
        int end = Math.min(start + pageRequest.getSize(), allLogs.size());

        List<StudyLog> pagedLogs = allLogs.subList(start, end);

        // 4. DTO 변환
        List<StudyLogResponse> responses = pagedLogs.stream()
                .map(StudyLogResponse::from)
                .collect(Collectors.toList());

        // 5. PageResponse 생성
        return PageResponse.of(
                responses,
                pageRequest.getPage(),
                pageRequest.getSize(),
                totalElements
        );
    }

    /**
     * 카테고리별 페이징 조회
     */
    public PageResponse<StudyLogResponse> getStudyLogsByCategoryWithPaging(
            String categoryName, PageRequest pageRequest) {

        Category category;
        try {
            category = Category.valueOf(categoryName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 카테고리: " + categoryName);
        }

        // 카테고리로 필터링
        List<StudyLog> filteredLogs = studyLogDao.findByCategory(category.toString());

        // 정렬
        filteredLogs.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        // 페이징 처리
        long totalElements = filteredLogs.size();
        int start = pageRequest.getOffset();
        int end = Math.min(start + pageRequest.getSize(), filteredLogs.size());

        List<StudyLog> pagedLogs = filteredLogs.subList(start, end);

        // DTO 변환
        List<StudyLogResponse> responses = pagedLogs.stream()
                .map(StudyLogResponse::from)
                .collect(Collectors.toList());

        return PageResponse.of(
                responses,
                pageRequest.getPage(),
                pageRequest.getSize(),
                totalElements
        );
    }

    /**
     * 학습 일지 총 개수를 반환합니다.
     *
     * @return 학습 일지 총 개수
     */
    public long getStudyLogCount() {
        return studyLogDao.count();
    }

    // ==================== UPDATE ====================

    /**
     * 학습 일지 수정
     *
     * @param id      수정할 학습 일지 ID
     * @param request 수정 요청 데이터
     * @return 수정된 학습 일지 응답
     */
    public StudyLogResponse updateStudyLog(Long id, StudyLogUpdateRequest request) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(request);

        // 1. 기존 학습 일지 조회 (DAO 사용)
        StudyLog studyLog = studyLogDao.findById(id)
                .orElseThrow(() -> new StudyLogNotFoundException(id));

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

        // 6. 저장 및 응답 반환 (DAO 사용)
        StudyLog updatedStudyLog = studyLogDao.update(studyLog);
        return StudyLogResponse.from(updatedStudyLog);
    }

    /**
     * Map을 사용한 동적 업데이트 (Reflection 활용)
     * Enum 타입 변환 로직 추가
     */
    public StudyLogResponse updateV2StudyLog(Long id, Map<String, Object> request)
            throws NoSuchFieldException, IllegalAccessException {
        StudyLog prevStudyLog = studyLogDao.findById(id)
                .orElseThrow(() -> new StudyLogNotFoundException(id));

        for(Map.Entry<String, Object> entry : request.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            // Reflection
            // 1. 클래스의 정보를 읽어온다.
            Class<?> studyLogClass = prevStudyLog.getClass();
            if(value != null) {
                // 2. 해당 하는 클래스의 필드를 읽고(getDeclaredField)
                java.lang.reflect.Field field = studyLogClass.getDeclaredField(key);
                // 3. private 필드에 접근 가능하도록 설정
                field.setAccessible(true);

                // 4. 필드 타입 확인 및 변환
                Object convertedValue = value;
                Class<?> fieldType = field.getType();

                // Enum 타입인지 확인하고 변환
                if (fieldType.isEnum() && value instanceof String) {
                    String stringValue = (String) value;
                    // Enum.valueOf를 사용하여 String을 Enum으로 변환
                    @SuppressWarnings("unchecked")
                    Class<? extends Enum> enumType = (Class<? extends Enum>) fieldType;
                    try {
                        convertedValue = Enum.valueOf(enumType, stringValue.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException(
                            "유효하지 않은 " + fieldType.getSimpleName() + " 값: " + stringValue);
                    }
                }
                // LocalDate 타입 변환 (필요한 경우)
                else if (fieldType == LocalDate.class && value instanceof String) {
                    convertedValue = LocalDate.parse((String) value);
                }

                // 5. 해당 하는 필드의 값을 변환된 value로 set 한다.(set)
                field.set(prevStudyLog, convertedValue);
            }
        }
        return StudyLogResponse.from(studyLogDao.update(prevStudyLog));
    }

    // ==================== DELETE ====================

    /**
     * 학습 일지를 삭제합니다.
     *
     * @param id 삭제할 학습 일지 ID
     * @return 삭제 결과 응답
     * @throws StudyLogNotFoundException 해당 ID의 학습 일지가 없는 경우
     */
    public StudyLogDeleteResponse deleteStudyLog(Long id) {
        // 1. 존재 여부 확인 (DAO 사용)
        if (!studyLogDao.existsById(id)) {
            throw new StudyLogNotFoundException(id);
        }

        // 2. 삭제 수행 (DAO 사용)
        studyLogDao.deleteById(id);

        // 3. 삭제 결과 반환
        return StudyLogDeleteResponse.of(id);
    }

    // ==================== VALIDATION ====================

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
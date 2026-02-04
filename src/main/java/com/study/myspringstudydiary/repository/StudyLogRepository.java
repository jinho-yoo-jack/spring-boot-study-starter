package com.study.myspringstudydiary.repository;

import com.study.myspringstudydiary.dto.request.PageRequest;
import com.study.myspringstudydiary.dto.response.PageResponse;
import com.study.myspringstudydiary.entity.Category;
import com.study.myspringstudydiary.entity.StudyLog;
import com.study.myspringstudydiary.exception.InvalidPageRequestException;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 학습 일지 저장소
 *
 * @Repository 어노테이션 설명:
 * - 이 클래스를 Spring Bean으로 등록합니다
 * - 데이터 접근 계층임을 명시합니다
 * - 데이터 접근 관련 예외를 Spring의 DataAccessException으로 변환해줍니다
 * <p>
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
     *
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
     *
     * @return 모든 학습 일지 리스트
     */
    public List<StudyLog> findAll() {
        // Map의 모든 값을 리스트로 변환하여 반환
        return database.values().stream()
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .sorted(Comparator.comparing(StudyLog::getCreatedAt))
                .collect(Collectors.toList());
    }

    /**
     * ID로 학습 일지 조회
     *
     * @param id 조회할 학습 일지 ID
     * @return 학습 일지 (없으면 null)
     */
    public StudyLog findById(Long id) {
        // Map에서 ID로 조회
        return database.get(id);
    }

    /**
     * 날짜별 학습 일지 조회
     *
     * @param date 조회할 날짜
     * @return 해당 날짜의 학습 일지 리스트
     */
    public List<StudyLog> findByDate(LocalDate date) {
        return database.values().stream()
                .filter(log -> log.getStudyDate().equals(date))
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .collect(Collectors.toList());
    }

    /**
     * 카테고리별 학습 일지 조회
     *
     * @param category 조회할 카테고리
     * @return 해당 카테고리의 학습 일지 리스트
     */
    public List<StudyLog> findByCategory(Category category) {
        return database.values().stream()
                .filter(log -> log.getCategory().equals(category))
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .collect(Collectors.toList());
    }

    /**
     * 페이징 처리된 학습 일지 조회
     *
     * @param pageRequest 페이징 요청 정보
     * @return 페이징 처리된 결과
     */
    public PageResponse<StudyLog> findAllWithPaging(PageRequest pageRequest) {

        // 1. 전체 데이터를 정렬
        List<StudyLog> allLogs = database.values().stream()
                .sorted((a, b) -> {
                    // 정렬 기준에 따라 정렬
                    int result = switch (pageRequest.getSortBy()) {
                        case "title" -> a.getTitle().compareTo(b.getTitle());
                        case "studyTime" -> a.getStudyTime().compareTo(b.getStudyTime());
                        case "studyDate" -> a.getStudyDate().compareTo(b.getStudyDate());
                        default -> a.getCreatedAt().compareTo(b.getCreatedAt());
                    };

                    // 정렬 방향 적용
                    return "ASC".equals(pageRequest.getSortDirection()) ? result : -result;
                })
                .collect(Collectors.toList());

        // 2. 전체 개수
        long totalElements = allLogs.size();

        // 3. 총 페이지 수 계산
        int totalPages = calculateTotalPages(totalElements, pageRequest.getSize());

        // 4. 요청한 페이지 번호 유효성 검증
        int requestedPage = pageRequest.getPage();

        if (requestedPage < 0) {
            throw new InvalidPageRequestException(requestedPage, totalPages);
        }

        if (totalElements > 0 && requestedPage >= totalPages) {
            throw new InvalidPageRequestException(requestedPage, totalPages);
        }

        // 5. 페이징 적용
        int start = pageRequest.getOffset();
        int end = Math.min(start + pageRequest.getSize(), allLogs.size());

        List<StudyLog> pagedLogs = allLogs.subList(start, end);

        // 6. PageResponse 생성
        return PageResponse.of(
                pagedLogs,
                pageRequest.getPage(),
                pageRequest.getSize(),
                totalElements
        );
    }

    /**
     * 카테고리별 페이징 조회
     *
     * @param category    카테고리
     * @param pageRequest 페이징 요청 정보
     * @return 페이징 처리된 결과
     */
    public PageResponse<StudyLog> findByCategoryWithPaging(Category category,
                                                           PageRequest pageRequest) {

        // 1. 카테고리로 필터링 및 정렬
        List<StudyLog> filteredLogs = database.values().stream()
                .filter(log -> log.getCategory() == category)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());

        // 2. 전체 개수
        long totalElements = filteredLogs.size();

        // 3. 총 페이지 수 계산
        int totalPages = calculateTotalPages(totalElements, pageRequest.getSize());

        // 4. 요청한 페이지 번호 유효성 검증
        int requestedPage = pageRequest.getPage();

        if (requestedPage < 0) {
            throw new InvalidPageRequestException(requestedPage, totalPages);
        }

        if (totalElements > 0 && requestedPage >= totalPages) {
            throw new InvalidPageRequestException(requestedPage, totalPages);
        }

        // 5. 페이징 적용
        int start = pageRequest.getOffset();
        int end = Math.min(start + pageRequest.getSize(), filteredLogs.size());

        List<StudyLog> pagedLogs = filteredLogs.subList(start, end);

        // 6. PageResponse 생성
        return PageResponse.of(
                pagedLogs,
                pageRequest.getPage(),
                pageRequest.getSize(),
                totalElements
        );
    }

    /**
     * 총 페이지 수 계산
     *
     * @param totalElements 전체 데이터 개수
     * @param pageSize      페이지 크기
     * @return 총 페이지 수
     */
    private int calculateTotalPages(long totalElements, int pageSize) {
        return (int) Math.ceil((double) totalElements / pageSize);
    }
}
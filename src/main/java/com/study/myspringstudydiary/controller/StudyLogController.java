package com.study.myspringstudydiary.controller;

import com.study.myspringstudydiary.global.common.Page;
import com.study.myspringstudydiary.dto.request.StudyLogCreateRequest;
import com.study.myspringstudydiary.dto.request.StudyLogUpdateRequest;
import com.study.myspringstudydiary.dto.response.StudyLogResponse;
import com.study.myspringstudydiary.dto.response.StudyLogDeleteResponse;
import com.study.myspringstudydiary.service.StudyLogService;
import com.study.myspringstudydiary.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 학습 일지 컨트롤러 with Lombok
 *
 * @RestController 어노테이션 설명:
 * - @Controller + @ResponseBody 의 조합
 * - 이 클래스의 모든 메서드 반환값을 JSON으로 변환하여 응답
 * - REST API 개발 시 사용
 *
 * @RequestMapping 어노테이션 설명:
 * - 이 컨트롤러의 기본 URL 경로를 설정
 * - 모든 메서드의 URL 앞에 "/api/v1/logs"가 붙음
 *
 * @RequiredArgsConstructor:
 * - final 필드에 대한 생성자를 자동으로 생성
 * - 의존성 주입을 위한 코드를 간결하게 만들어줌
 *
 * @Slf4j:
 * - SLF4J 로거를 자동으로 생성 (log 변수 사용 가능)
 */
@Slf4j
@RestController
@RequiredArgsConstructor  // Lombok이 생성자를 자동 생성
@RequestMapping("/api/v1/logs")
@Validated  // PathVariable, RequestParam 검증을 위해 추가
public class StudyLogController {

    private final StudyLogService studyLogService;
    // 생성자는 @RequiredArgsConstructor가 자동으로 생성

    // ========== CREATE ==========

    /**
     * 학습 일지 생성 (CREATE)
     *
     * @PostMapping: POST 요청을 처리
     * @RequestBody: HTTP Body의 JSON을 객체로 변환
     *
     * POST /api/v1/logs
     */
    @PostMapping
    public ResponseEntity<ApiResponse<StudyLogResponse>> createStudyLog(
            @Valid @RequestBody StudyLogCreateRequest request) {

        log.info("POST /api/v1/logs - Creating study log: {}", request.getTitle());

        // Service 호출하여 학습 일지 생성
        StudyLogResponse response = studyLogService.createStudyLog(request);

        log.info("Study log created successfully with ID: {}", response.getId());

        // 201 Created 상태 코드와 함께 응답
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    // ========== READ ==========

    /**
     * 모든 학습 일지 조회 (READ - All)
     *
     * @GetMapping: GET 요청을 처리
     *
     * GET /api/v1/logs
     */
    @GetMapping
    public List<StudyLogResponse> getAllStudyLogs() {
        return studyLogService.getAllStudyLogs();
    }

    /**
     * 특정 학습 일지 조회 (READ - Single)
     *
     * @GetMapping("/{id}"): GET 요청을 처리 (경로 변수 포함)
     * @PathVariable: URL 경로의 {id} 값을 매개변수로 받음
     *
     * GET /api/v1/logs/{id}
     */
    @GetMapping("/{id}")
    public StudyLogResponse getStudyLogById(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long id) {

        return studyLogService.getStudyLogById(id);
    }

    /**
     * 날짜로 학습 일지 조회
     *
     * GET /api/v1/logs/date/{date}
     *
     * @param date 조회할 날짜 (yyyy-MM-dd 형식)
     * @return 해당 날짜의 학습 일지 리스트
     */
    @GetMapping("/date/{date}")
    public List<StudyLogResponse> getStudyLogsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return studyLogService.getStudyLogsByDate(date);
    }

    /**
     * 카테고리로 학습 일지 조회
     *
     * GET /api/v1/logs/category/{category}
     *
     * @param category 조회할 카테고리 (SPRING, DATABASE, JAVA, WEB, ALGORITHM, ETC)
     * @return 해당 카테고리의 학습 일지 리스트
     */
    @GetMapping("/category/{category}")
    public List<StudyLogResponse> getStudyLogsByCategory(
            @PathVariable String category) {

        return studyLogService.getStudyLogsByCategoryString(category);
    }

    /**
     * 오늘의 학습 일지 조회
     *
     * GET /api/v1/logs/today
     *
     * @return 오늘 작성된 학습 일지 리스트
     */
    @GetMapping("/today")
    public List<StudyLogResponse> getTodayStudyLogs() {
        return studyLogService.getStudyLogsByDate(LocalDate.now());
    }

    // ========== PAGING ==========

    /**
     * 전체 학습 일지 페이징 조회
     *
     * GET /api/v1/logs/page?page=0&size=10
     * GET /api/v1/logs/page (기본값: page=0, size=10)
     *
     * @param page 페이지 번호 (0-based, 기본값: 0)
     * @param size 페이지 크기 (기본값: 10, 최대: 100)
     * @return 페이징된 학습 일지
     */
    @GetMapping("/page")
    public Page<StudyLogResponse> getStudyLogsPage(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
            @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다") int size) {
        return studyLogService.getStudyLogsWithPaging(page, size);
    }

    /**
     * 카테고리별 학습 일지 페이징 조회
     *
     * GET /api/v1/logs/category/{category}/page?page=0&size=10
     *
     * @param category 카테고리
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 페이징된 학습 일지
     */
    @GetMapping("/category/{category}/page")
    public Page<StudyLogResponse> getStudyLogsByCategoryPage(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return studyLogService.getStudyLogsByCategoryWithPaging(category, page, size);
    }


    /**
     * 검색 + 페이징 조회
     *
     * GET /api/v1/logs/search?title=Spring&category=SPRING
     *     &startDate=2026-01-01&endDate=2026-12-31
     *     &page=0&size=10
     *
     * @param title 제목 키워드 (선택)
     * @param category 카테고리 (선택)
     * @param startDate 시작 날짜 (선택)
     * @param endDate 종료 날짜 (선택)
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 페이징된 검색 결과
     */
    @GetMapping("/search")
    public Page<StudyLogResponse> searchStudyLogs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String category,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return studyLogService.searchStudyLogsWithPaging(
                title, category, startDate, endDate, page, size);
    }

    /**
     * 카테고리별 학습 일지 개수 조회
     *
     * GET /api/v1/logs/category/{category}/count
     *
     * @param category 조회할 카테고리
     * @return 카테고리명과 개수를 포함한 Map
     */
    @GetMapping("/category/{category}/count")
    public Map<String, Object> getStudyLogCountByCategory(
            @PathVariable String category) {

        long count = studyLogService.getStudyLogCountByCategory(category);

        Map<String, Object> response = new HashMap<>();
        response.put("category", category);
        response.put("count", count);

        return response;
    }

    // ========== UPDATE ==========

    /**
     * 학습 일지 수정
     * PUT /api/v1/logs/{id}
     *
     * @PutMapping: PUT 요청을 처리하는 어노테이션
     *              리소스의 전체 또는 일부를 수정할 때 사용
     *
     * @PathVariable: URL의 {id} 부분을 파라미터로 받음
     * @RequestBody: HTTP Body의 JSON을 객체로 변환
     */
    @PutMapping("/{id}")
    public StudyLogResponse updateStudyLog(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long id,
            @Valid @RequestBody StudyLogUpdateRequest request) {

        return studyLogService.updateStudyLog(id, request);
    }

    // ========== DELETE ==========

    /**
     * 학습 일지 삭제 API
     *
     * DELETE /api/v1/logs/{id}
     *
     * @param id 삭제할 학습 일지 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/{id}")
    public StudyLogDeleteResponse deleteStudyLog(
            @PathVariable @Positive(message = "ID는 양수여야 합니다") Long id) {

        return studyLogService.deleteStudyLog(id);
    }
}
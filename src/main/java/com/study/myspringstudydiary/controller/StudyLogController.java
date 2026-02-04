package com.study.myspringstudydiary.controller;

import com.study.myspringstudydiary.dto.request.StudyLogCreateRequest;
import com.study.myspringstudydiary.dto.request.StudyLogUpdateRequest;
import com.study.myspringstudydiary.dto.response.StudyLogResponse;
import com.study.myspringstudydiary.service.StudyLogService;
import com.study.myspringstudydiary.global.common.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 학습 일지 컨트롤러
 *
 * @RestController 어노테이션 설명:
 * - @Controller + @ResponseBody 의 조합
 * - 이 클래스의 모든 메서드 반환값을 JSON으로 변환하여 응답
 * - REST API 개발 시 사용
 * @RequestMapping 어노테이션 설명:
 * - 이 컨트롤러의 기본 URL 경로를 설정
 * - 모든 메서드의 URL 앞에 "/api/v1/logs"가 붙음
 */
@RestController  // ⭐ REST API 컨트롤러로 등록!
@RequestMapping("/api/v1/logs")  // 기본 URL 경로 설정
public class StudyLogController {

    private final StudyLogService studyLogService;

    public StudyLogController(StudyLogService studyLogService) {
        this.studyLogService = studyLogService;
    }

    // ==================== CREATE (Day 1) ====================

    @PostMapping
    public StudyLogResponse createStudyLog(
            @RequestBody StudyLogCreateRequest request) {
        StudyLogResponse response = studyLogService.createStudyLog(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    // ==================== READ (Day 2) ====================

    @GetMapping
    public List<StudyLogResponse> getAllStudyLogs() {
        List<StudyLogResponse> responses = studyLogService.getAllStudyLogs();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    public StudyLogResponse getStudyLogById(
            @PathVariable Long id) {
        StudyLogResponse response = studyLogService.getStudyLogById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponse<List<StudyLogResponse>>> getStudyLogsByDate(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        List<StudyLogResponse> responses = studyLogService.getStudyLogsByDate(date);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/category/{category}")
    public List<StudyLogResponse> getStudyLogsByCategory(
            @PathVariable String category) {
        List<StudyLogResponse> responses = studyLogService.getStudyLogsByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // ==================== UPDATE (Day 3 - 오늘!) ====================

    /**
     * 학습 일지 수정
     * PUT /api/v1/logs/{id}
     *
     * @PutMapping: PUT 요청을 처리하는 어노테이션
     * 리소스의 전체 또는 일부를 수정할 때 사용
     * @PathVariable: URL의 {id} 부분을 파라미터로 받음
     * @RequestBody: HTTP Body의 JSON을 객체로 변환
     */
    @PutMapping("/{id}")
    public StudyLogResponse updateStudyLog(
            @PathVariable Long id,
            @RequestBody StudyLogUpdateRequest request) {

        StudyLogResponse response = studyLogService.updateStudyLog(id, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
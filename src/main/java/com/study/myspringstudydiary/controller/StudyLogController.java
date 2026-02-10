package com.study.myspringstudydiary.controller;

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

import java.util.List;

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
public class StudyLogController {

    private final StudyLogService studyLogService;
    // 생성자는 @RequiredArgsConstructor가 자동으로 생성

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
            @RequestBody StudyLogCreateRequest request) {

        log.info("POST /api/v1/logs - Creating study log: {}", request.getTitle());

        // Service 호출하여 학습 일지 생성
        StudyLogResponse response = studyLogService.createStudyLog(request);

        log.info("Study log created successfully with ID: {}", response.getId());

        // 201 Created 상태 코드와 함께 응답
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 모든 학습 일지 조회 (READ - All)
     *
     * @GetMapping: GET 요청을 처리
     *
     * GET /api/v1/logs
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<StudyLogResponse>>> getAllStudyLogs() {

        // Service 호출하여 모든 학습 일지 조회
        List<StudyLogResponse> responses = studyLogService.getAllStudyLogs();

        // 200 OK 상태 코드와 함께 응답
        return ResponseEntity
                .ok()
                .body(ApiResponse.success(responses));
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
    public ResponseEntity<ApiResponse<StudyLogResponse>> getStudyLogById(
            @PathVariable Long id) {

        // Service 호출하여 ID로 학습 일지 조회
        StudyLogResponse response = studyLogService.getStudyLogById(id);

        // 200 OK 상태 코드와 함께 응답
        return ResponseEntity
                .ok()
                .body(ApiResponse.success(response));
    }

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
    public ResponseEntity<ApiResponse<StudyLogResponse>> updateStudyLog(
            @PathVariable Long id,
            @RequestBody StudyLogUpdateRequest request) {

        StudyLogResponse response = studyLogService.updateStudyLog(id, request);

        return ResponseEntity.ok(ApiResponse.success(response));
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
    public ResponseEntity<ApiResponse<StudyLogDeleteResponse>> deleteStudyLog(
            @PathVariable Long id) {

        StudyLogDeleteResponse response = studyLogService.deleteStudyLog(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
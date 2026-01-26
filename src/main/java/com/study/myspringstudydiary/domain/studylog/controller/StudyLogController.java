package com.study.myspringstudydiary.domain.studylog.controller;

import com.study.myspringstudydiary.domain.studylog.dto.request.StudyLogCreateRequest;
import com.study.myspringstudydiary.domain.studylog.dto.response.StudyLogResponse;
import com.study.myspringstudydiary.domain.studylog.service.StudyLogService;
import com.study.myspringstudydiary.global.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 학습 일지 컨트롤러
 *
 * @RestController 어노테이션 설명:
 * - @Controller + @ResponseBody 의 조합
 * - 이 클래스의 모든 메서드 반환값을 JSON으로 변환하여 응답
 * - REST API 개발 시 사용
 *
 * @RequestMapping 어노테이션 설명:
 * - 이 컨트롤러의 기본 URL 경로를 설정
 * - 모든 메서드의 URL 앞에 "/api/v1/logs"가 붙음
 */
@RestController  // ⭐ REST API 컨트롤러로 등록!
@RequestMapping("/api/v1/logs")  // 기본 URL 경로 설정
public class StudyLogController {

    // ⭐ 의존성 주입: Service를 주입받음
    private final StudyLogService studyLogService;

    /**
     * 생성자 주입
     * Spring이 StudyLogService Bean을 찾아서 자동으로 주입해줍니다.
     */
    public StudyLogController(StudyLogService studyLogService) {
        this.studyLogService = studyLogService;
    }

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

        // Service 호출하여 학습 일지 생성
        StudyLogResponse response = studyLogService.createStudyLog(request);

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
}
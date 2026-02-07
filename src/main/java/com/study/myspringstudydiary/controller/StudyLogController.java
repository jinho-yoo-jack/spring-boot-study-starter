package com.study.myspringstudydiary.controller;

import com.study.myspringstudydiary.dto.request.StudyLogCreateRequest;
import com.study.myspringstudydiary.dto.request.StudyLogUpdateRequest;
import com.study.myspringstudydiary.dto.response.StudyLogResponse;
import com.study.myspringstudydiary.dto.response.StudyLogDeleteResponse;
import com.study.myspringstudydiary.service.StudyLogService;
import com.study.myspringstudydiary.entity.Category;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
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
    public StudyLogResponse createStudyLog(
            @RequestBody StudyLogCreateRequest request) {

        // Service 호출하여 학습 일지 생성
        return studyLogService.createStudyLog(request);
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
            @PathVariable Long id) {

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
            @PathVariable Long id,
            @RequestBody StudyLogUpdateRequest request) {

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
            @PathVariable Long id) {

        return studyLogService.deleteStudyLog(id);
    }
}
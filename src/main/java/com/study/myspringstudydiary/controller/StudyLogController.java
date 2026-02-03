package com.study.myspringstudydiary.controller;

import com.study.myspringstudydiary.dto.request.StudyLogCreateRequest;
import com.study.myspringstudydiary.dto.response.StudyLogResponse;
import com.study.myspringstudydiary.service.StudyLogService;
import com.study.myspringstudydiary.global.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public StudyLogResponse createStudyLog(
            @RequestBody StudyLogCreateRequest request) {

        // Service 호출하여 학습 일지 생성
        return studyLogService.createStudyLog(request);
    }
}
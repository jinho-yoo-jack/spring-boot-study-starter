package com.study.myspringstudydiary.study_log.controller;

import com.study.myspringstudydiary.global.common.ApiResponse;
import com.study.myspringstudydiary.global.common.Page;
import com.study.myspringstudydiary.study_log.dto.request.StudyLogCreateRequest;
import com.study.myspringstudydiary.study_log.dto.request.StudyLogUpdateRequest;
import com.study.myspringstudydiary.study_log.dto.response.StudyLogDeleteResponse;
import com.study.myspringstudydiary.study_log.dto.response.StudyLogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Study Log API Documentation Interface
 * 학습 기록 관련 API 문서화를 위한 인터페이스
 */
@Tag(name = "학습 기록", description = "학습 기록 CRUD API - JWT 인증 필요")
public interface StudyLogControllerApi {

    @Operation(
            summary = "학습 기록 생성",
            description = """
                    새로운 학습 기록을 생성합니다.

                    ### 검증 규칙
                    - **title**: 필수, 1-100자
                    - **content**: 필수, 1-1000자
                    - **category**: 필수, JAVA/SPRING/JPA/DATABASE/ALGORITHM/CS/NETWORK/GIT/ETC 중 선택
                    - **understanding**: 필수, VERY_GOOD/GOOD/NORMAL/BAD/VERY_BAD 중 선택
                    - **studyTime**: 필수, 1-1440분 (1분~24시간)
                    - **studyDate**: 선택, 생략 시 현재 날짜

                    ### 주의사항
                    - 중복된 제목도 허용됩니다
                    - JWT 토큰 인증이 필요합니다
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "id": 1,
                                                "title": "Spring Security JWT 인증",
                                                "content": "JWT 토큰 생성 및 검증 로직 구현 완료",
                                                "category": "SPRING",
                                                "categoryIcon": "🌱",
                                                "understanding": "GOOD",
                                                "understandingEmoji": "😊",
                                                "studyTime": 120,
                                                "studyDate": "2024-01-15",
                                                "createdAt": "2024-01-15T10:30:00",
                                                "updatedAt": "2024-01-15T10:30:00"
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "입력값 검증 실패",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": false,
                                              "data": null,
                                              "error": {
                                                "code": "VALIDATION_ERROR",
                                                "message": "학습 주제는 필수입니다"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - 토큰 없음 또는 만료"
            )
    })
    ResponseEntity<ApiResponse<StudyLogResponse>> createStudyLog(
            @RequestBody(
                    description = "학습 기록 생성 요청 데이터",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = StudyLogCreateRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "title": "JPA N+1 문제 해결",
                                              "content": "Fetch Join과 EntityGraph를 사용하여 N+1 문제를 해결했습니다.",
                                              "category": "JPA",
                                              "understanding": "GOOD",
                                              "studyTime": 90,
                                              "studyDate": "2024-01-15"
                                            }
                                            """
                            )
                    )
            )
            @Valid StudyLogCreateRequest request
    );

    @Operation(
            summary = "전체 학습 기록 조회",
            description = "모든 학습 기록을 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            )
    })
    List<StudyLogResponse> getAllStudyLogs();

    @Operation(
            summary = "학습 기록 단건 조회",
            description = "ID로 특정 학습 기록을 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "학습 기록을 찾을 수 없음"
            )
    })
    StudyLogResponse getStudyLogById(
            @Parameter(description = "학습 기록 ID", required = true)
            @Positive(message = "ID는 양수여야 합니다") Long id
    );

    @Operation(
            summary = "날짜별 학습 기록 조회",
            description = "특정 날짜의 학습 기록을 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            )
    })
    List<StudyLogResponse> getStudyLogsByDate(
            @Parameter(description = "조회할 날짜 (yyyy-MM-dd)", required = true)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    );

    @Operation(
            summary = "카테고리별 학습 기록 조회",
            description = "특정 카테고리의 학습 기록을 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            )
    })
    List<StudyLogResponse> getStudyLogsByCategory(
            @Parameter(description = "카테고리 (SPRING, DATABASE, JAVA, WEB, ALGORITHM, ETC)", required = true)
            String category
    );

    @Operation(
            summary = "오늘의 학습 기록 조회",
            description = "오늘 작성된 학습 기록을 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            )
    })
    List<StudyLogResponse> getTodayStudyLogs();

    @Operation(
            summary = "학습 기록 페이징 조회",
            description = "전체 학습 기록을 페이징하여 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            )
    })
    Page<StudyLogResponse> getStudyLogsPage(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다") int page,
            @Parameter(description = "페이지 크기 (1-100)", example = "10")
            @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
            @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다") int size
    );

    @Operation(
            summary = "카테고리별 학습 기록 페이징 조회",
            description = "특정 카테고리의 학습 기록을 페이징하여 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            )
    })
    Page<StudyLogResponse> getStudyLogsByCategoryPage(
            @Parameter(description = "카테고리", required = true) String category,
            @Parameter(description = "페이지 번호", example = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") int size
    );

    @Operation(
            summary = "학습 기록 검색",
            description = "다양한 조건으로 학습 기록을 검색하고 페이징합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "검색 성공"
            )
    })
    Page<StudyLogResponse> searchStudyLogs(
            @Parameter(description = "제목 키워드") String title,
            @Parameter(description = "카테고리") String category,
            @Parameter(description = "시작 날짜") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료 날짜") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "페이지 번호", example = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") int size
    );

    @Operation(
            summary = "카테고리별 학습 기록 개수 조회",
            description = "특정 카테고리의 학습 기록 개수를 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            )
    })
    Map<String, Object> getStudyLogCountByCategory(
            @Parameter(description = "카테고리", required = true) String category
    );

    @Operation(
            summary = "학습 기록 수정",
            description = "기존 학습 기록을 수정합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "수정 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "학습 기록을 찾을 수 없음"
            )
    })
    StudyLogResponse updateStudyLog(
            @Parameter(description = "학습 기록 ID", required = true)
            @Positive(message = "ID는 양수여야 합니다") Long id,
            @RequestBody(
                    description = "학습 기록 수정 요청 데이터",
                    required = true,
                    content = @Content(schema = @Schema(implementation = StudyLogUpdateRequest.class))
            )
            @Valid StudyLogUpdateRequest request
    );

    @Operation(
            summary = "학습 기록 삭제",
            description = "학습 기록을 삭제합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "학습 기록을 찾을 수 없음"
            )
    })
    StudyLogDeleteResponse deleteStudyLog(
            @Parameter(description = "학습 기록 ID", required = true)
            @Positive(message = "ID는 양수여야 합니다") Long id
    );
}
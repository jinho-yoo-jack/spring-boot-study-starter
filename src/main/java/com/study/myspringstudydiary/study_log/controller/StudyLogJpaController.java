package com.study.myspringstudydiary.study_log.controller;

import com.study.myspringstudydiary.global.security.annotation.CurrentUser;
import com.study.myspringstudydiary.global.security.principal.UserPrincipal;
import com.study.myspringstudydiary.study_log.dto.request.StudyLogCreateRequest;
import com.study.myspringstudydiary.study_log.dto.request.StudyLogUpdateRequest;
import com.study.myspringstudydiary.study_log.dto.response.StudyLogResponse;
import com.study.myspringstudydiary.study_log.dto.response.StudyLogDeleteResponse;
import com.study.myspringstudydiary.study_log.entity.Category;
import com.study.myspringstudydiary.study_log.entity.Understanding;
import com.study.myspringstudydiary.study_log.service.StudyLogJpaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Study Log Controller with Spring Data JPA
 * 페이징 제거 버전
 */
@Slf4j
@Tag(name = "학습 기록 (JPA)", description = "Spring Data JPA를 활용한 학습 기록 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/study-logs")
@Validated
public class StudyLogJpaController {

    private final StudyLogJpaService studyLogService;

    // ========== CREATE ==========

    @Operation(summary = "학습 기록 생성", description = "새로운 학습 기록을 생성합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<StudyLogResponse> create(
            @Valid @RequestBody StudyLogCreateRequest request,
            @CurrentUser UserPrincipal currentUser) {

        log.info("Creating study log: {} for user: {}", request.getTitle(), currentUser.getUsername());
        Long userId = currentUser.getId();
        StudyLogResponse created = studyLogService.create(userId, request);

        return ResponseEntity
                .created(URI.create("/api/v2/study-logs/" + created.getId()))
                .body(created);
    }

    // ========== READ ==========

    @Operation(summary = "전체 학습 기록 조회", description = "모든 학습 기록을 조회합니다.")
    @GetMapping
    public List<StudyLogResponse> getStudyLogs() {
        log.info("Getting all study logs");
        return studyLogService.findAll();
    }

    @Operation(summary = "카테고리별 조회", description = "특정 카테고리의 학습 기록을 조회합니다.")
    @GetMapping("/category/{category}")
    public List<StudyLogResponse> getByCategory(@PathVariable Category category) {
        return studyLogService.findByCategory(category);
    }

    @Operation(summary = "이해도별 조회", description = "특정 이해도의 학습 기록을 조회합니다.")
    @GetMapping("/understanding/{understanding}")
    public List<StudyLogResponse> getByUnderstanding(@PathVariable Understanding understanding) {
        return studyLogService.findByUnderstanding(understanding);
    }

    @Operation(summary = "날짜 범위 검색", description = "날짜 범위를 지정하여 학습 기록을 검색합니다.")
    @GetMapping("/date-range")
    public List<StudyLogResponse> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return studyLogService.findByDateRange(startDate, endDate);
    }

    @Operation(summary = "다중 조건 검색", description = "여러 조건으로 학습 기록을 검색합니다.")
    @GetMapping("/search")
    public List<StudyLogResponse> search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @CurrentUser UserPrincipal currentUser) {
        Long userId = currentUser.getId();
        return studyLogService.search(userId, title, category, startDate, endDate);
    }

    @Operation(summary = "전문 검색", description = "MySQL Full-text search를 사용한 전문 검색")
    @GetMapping("/fulltext")
    public List<StudyLogResponse> searchFullText(@RequestParam String keyword) {
        return studyLogService.searchFullText(keyword);
    }

    @Operation(summary = "단건 조회", description = "ID로 학습 기록을 조회합니다.")
    @GetMapping("/{id}")
    public StudyLogResponse getById(@PathVariable Long id) {
        return studyLogService.findById(id);
    }

    @Operation(summary = "오늘의 학습 기록", description = "오늘 작성된 학습 기록을 조회합니다.")
    @GetMapping("/today")
    public List<StudyLogResponse> getTodayStudyLogs() {
        return studyLogService.findTodayStudyLogs();
    }

    // ========== UPDATE ==========

    @Operation(summary = "학습 기록 수정", description = "기존 학습 기록을 수정합니다.")
    @PutMapping("/{id}")
    public StudyLogResponse update(
            @PathVariable Long id,
            @Valid @RequestBody StudyLogUpdateRequest request,
            @CurrentUser UserPrincipal currentUser) {
        return studyLogService.update(id, currentUser.getId(), request);
    }

    @Operation(summary = "벌크 시간 업데이트", description = "여러 학습 기록의 시간을 일괄 수정합니다.")
    @PatchMapping("/bulk-hours")
    public ResponseEntity<Void> bulkUpdateHours(
            @RequestBody List<Long> ids,
            @RequestParam Integer hours) {
        studyLogService.bulkUpdateHours(ids, hours);
        return ResponseEntity.noContent().build();
    }

    // ========== DELETE ==========

    @Operation(summary = "학습 기록 삭제", description = "학습 기록을 삭제합니다.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @CurrentUser UserPrincipal currentUser) {
        studyLogService.delete(id, currentUser.getId());
    }

    // ========== STATISTICS ==========

    @Operation(summary = "카테고리별 개수", description = "특정 카테고리의 학습 기록 개수를 조회합니다.")
    @GetMapping("/count/{category}")
    public Map<String, Object> countByCategory(@PathVariable Category category) {
        long count = studyLogService.countByCategory(category);
        return Map.of(
            "category", category,
            "count", count
        );
    }

    @Operation(summary = "카테고리별 학습 시간 통계", description = "각 카테고리별 총 학습 시간을 조회합니다.")
    @GetMapping("/stats/time-by-category")
    public List<Object[]> getStudyTimeByCategory() {
        return studyLogService.getStudyTimeByCategory();
    }

    // ========== CACHE ==========

    @Operation(summary = "캐시 무효화", description = "카테고리 카운트 캐시를 무효화합니다.")
    @PostMapping("/cache/evict")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void evictCache() {
        studyLogService.evictCache();
    }

    // ========== USER-SPECIFIC ENDPOINTS ==========

    @Operation(summary = "내 학습 기록 조회", description = "현재 로그인한 사용자의 모든 학습 기록을 조회합니다.")
    @GetMapping("/my")
    public List<StudyLogResponse> getMyStudyLogs(@CurrentUser UserPrincipal currentUser) {
        log.info("Fetching study logs for user: {}", currentUser.getUsername());
        return studyLogService.findByUserId(currentUser.getId());
    }

    @Operation(summary = "내 카테고리별 학습 기록", description = "현재 로그인한 사용자의 특정 카테고리 학습 기록을 조회합니다.")
    @GetMapping("/my/category/{category}")
    public List<StudyLogResponse> getMyStudyLogsByCategory(
            @PathVariable Category category,
            @CurrentUser UserPrincipal currentUser) {
        return studyLogService.findByUserAndCategory(currentUser.getId(), category);
    }

    @Operation(summary = "내 날짜 범위 학습 기록", description = "현재 로그인한 사용자의 날짜 범위별 학습 기록을 조회합니다.")
    @GetMapping("/my/date-range")
    public List<StudyLogResponse> getMyStudyLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @CurrentUser UserPrincipal currentUser) {
        return studyLogService.findByUserIdAndDateRange(currentUser.getId(), startDate, endDate);
    }

    @Operation(summary = "내 학습 기록 개수", description = "현재 로그인한 사용자의 총 학습 기록 개수를 조회합니다.")
    @GetMapping("/my/count")
    public Map<String, Long> getMyStudyLogCount(@CurrentUser UserPrincipal currentUser) {
        long count = studyLogService.countByUserId(currentUser.getId());
        return Map.of("userId", currentUser.getId(), "count", count);
    }

    @Operation(summary = "내 카테고리별 학습 시간 통계", description = "현재 로그인한 사용자의 카테고리별 학습 시간 통계를 조회합니다.")
    @GetMapping("/my/stats/time-by-category")
    public List<Object[]> getMyStudyTimeByCategory(@CurrentUser UserPrincipal currentUser) {
        return studyLogService.getStudyTimeByCategoryForUser(currentUser.getId());
    }
}
package com.study.myspringstudydiary.controller;

import com.study.myspringstudydiary.dto.DiaryRequest;
import com.study.myspringstudydiary.dto.DiaryResponse;
import com.study.myspringstudydiary.service.DiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    @PostMapping
    public ResponseEntity<DiaryResponse> createDiary(@Valid @RequestBody DiaryRequest request) {
        log.info("Creating diary for user id: {}", request.getUserId());
        DiaryResponse response = diaryService.createDiary(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiaryResponse> getDiaryById(@PathVariable Long id) {
        log.info("Fetching diary with id: {}", id);
        DiaryResponse response = diaryService.getDiaryById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<DiaryResponse>> getDiariesByUserId(
            @PathVariable Long userId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Fetching diaries for user id: {}", userId);
        Page<DiaryResponse> responses = diaryService.getDiariesByUserId(userId, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<List<DiaryResponse>> getDiariesByDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Fetching diaries for user id: {} between {} and {}", userId, startDate, endDate);
        List<DiaryResponse> responses = diaryService.getDiariesByUserIdAndDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<DiaryResponse>> searchDiaries(
            @RequestParam String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Searching diaries with keyword: {}", keyword);
        Page<DiaryResponse> responses = diaryService.searchDiaries(keyword, pageable);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiaryResponse> updateDiary(
            @PathVariable Long id,
            @Valid @RequestBody DiaryRequest request) {
        log.info("Updating diary with id: {}", id);
        DiaryResponse response = diaryService.updateDiary(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiary(@PathVariable Long id) {
        log.info("Deleting diary with id: {}", id);
        diaryService.deleteDiary(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Long> getDiaryCount(@PathVariable Long userId) {
        log.info("Counting diaries for user id: {}", userId);
        Long count = diaryService.getDiaryCountByUserId(userId);
        return ResponseEntity.ok(count);
    }
}
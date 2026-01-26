package com.study.myspringstudydiary.controller;

import com.study.myspringstudydiary.dto.DiaryRequest;
import com.study.myspringstudydiary.dto.DiaryResponse;
import com.study.myspringstudydiary.service.DiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    @PostMapping
    public DiaryResponse createDiary(@RequestBody DiaryRequest request) {
        log.info("Creating diary for user id: {}", request.getUserId());
        DiaryResponse response = diaryService.createDiary(request);
        return response;
    }
}
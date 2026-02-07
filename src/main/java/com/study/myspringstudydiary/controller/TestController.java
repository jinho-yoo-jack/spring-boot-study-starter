package com.study.myspringstudydiary.controller;

import com.study.myspringstudydiary.service.StudyLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TestController {

    private final StudyLogService studyLogService1;
    private final StudyLogService studyLogService2;

    public TestController(StudyLogService studyLogService1, StudyLogService studyLogService2) {
        this.studyLogService1 = studyLogService1;
        this.studyLogService2 = studyLogService2;
    }

    @GetMapping("/test")
    public Map<String, Boolean> test() {
        boolean isSame = studyLogService1 == studyLogService2;
        return Map.of("isSame", isSame);
    }
}

package com.study.myspringstudydiary.test;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Test", description = "테스트 API")
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Operation(summary = "테스트 엔드포인트", description = "Swagger가 작동하는지 확인하는 엔드포인트")
    @GetMapping("/hello")
    public String hello() {
        return "Hello, Swagger!";
    }
}
package com.study.myspringstudydiary.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController implements TestControllerApi {

    @Override
    @GetMapping("/hello")
    public String hello() {
        return "Hello, Swagger!";
    }
}
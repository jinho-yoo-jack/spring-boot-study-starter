package com.study.myspringstudydiary.controller;

import com.study.myspringstudydiary.dto.UserRequest;
import com.study.myspringstudydiary.dto.UserResponse;
import com.study.myspringstudydiary.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserResponse createUser(@RequestBody UserRequest request) {
        log.info("Creating user with username: {}", request.getUserName());
        UserResponse response = userService.createUser(request);
        return response;
    }
}
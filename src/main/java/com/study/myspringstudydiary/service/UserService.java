package com.study.myspringstudydiary.service;

import com.study.myspringstudydiary.dto.UserRequest;
import com.study.myspringstudydiary.dto.UserResponse;
import com.study.myspringstudydiary.exception.DuplicateResourceException;
import com.study.myspringstudydiary.model.User;
import com.study.myspringstudydiary.repository.UserMapRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapRepository userRepository;

    public UserResponse createUser(UserRequest request) {
        log.debug("Creating new user with username: {}", request.getUserName());

        // 중복 체크
        if (userRepository.existsByUserName(request.getUserName())) {
            throw new DuplicateResourceException("Username already exists: " + request.getUserName());
        }

        User user = User.builder()
                .userName(request.getUserName())
                .email(request.getEmail())
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created successfully with id: {}", savedUser.getId());

        return UserResponse.from(savedUser);
    }
}
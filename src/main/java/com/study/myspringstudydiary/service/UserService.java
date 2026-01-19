package com.study.myspringstudydiary.service;

import com.study.myspringstudydiary.dto.UserRequest;
import com.study.myspringstudydiary.dto.UserResponse;
import com.study.myspringstudydiary.entity.User;
import com.study.myspringstudydiary.exception.ResourceNotFoundException;
import com.study.myspringstudydiary.exception.DuplicateResourceException;
import com.study.myspringstudydiary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
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

    public UserResponse getUserById(Long id) {
        log.debug("Fetching user with id: {}", id);
        User user = userRepository.findByIdWithDiaries(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return UserResponse.from(user);
    }

    public UserResponse getUserByUserName(String userName) {
        log.debug("Fetching user with username: {}", userName);
        User user = userRepository.findByUserNameWithDiaries(userName)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + userName));
        return UserResponse.from(user);
    }

    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        log.debug("Updating user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // 다른 사용자가 이미 사용 중인 username인지 체크
        if (!user.getUserName().equals(request.getUserName()) &&
                userRepository.existsByUserName(request.getUserName())) {
            throw new DuplicateResourceException("Username already exists: " + request.getUserName());
        }

        user.updateUserName(request.getUserName());
        user.updateEmail(request.getEmail());

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with id: {}", updatedUser.getId());

        return UserResponse.from(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        log.debug("Deleting user with id: {}", id);

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully with id: {}", id);
    }
}
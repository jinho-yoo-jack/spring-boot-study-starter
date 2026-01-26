package com.study.myspringstudydiary.repository;

import com.study.myspringstudydiary.model.User;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserMapRepository {

    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public User save(User user) {
        if (user.getId() == null) {
            // 새 사용자 생성
            user.setId(idGenerator.getAndIncrement());
            user.setCreatedAt(LocalDateTime.now());
        }
        user.setUpdatedAt(LocalDateTime.now());
        users.put(user.getId(), user);
        return user;
    }

    public User findById(Long id) {
        return users.get(id);
    }

    public boolean existsByUserName(String userName) {
        return users.values().stream()
                .anyMatch(user -> user.getUserName().equals(userName));
    }
}
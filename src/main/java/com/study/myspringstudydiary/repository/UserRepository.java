package com.study.myspringstudydiary.repository;

import com.study.myspringstudydiary.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserName(String userName);

    boolean existsByUserName(String userName);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.diaries WHERE u.id = :id")
    Optional<User> findByIdWithDiaries(@Param("id") Long id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.diaries WHERE u.userName = :userName")
    Optional<User> findByUserNameWithDiaries(@Param("userName") String userName);
}
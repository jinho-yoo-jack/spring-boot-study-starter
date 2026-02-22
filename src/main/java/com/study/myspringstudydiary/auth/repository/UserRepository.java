package com.study.myspringstudydiary.auth.repository;

import com.study.myspringstudydiary.auth.entity.User;
import com.study.myspringstudydiary.auth.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * User Repository Interface
 * Spring Data JPA repository for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Query Method - 이메일로 사용자 찾기
    Optional<User> findByEmail(String email);

    // Query Method - 사용자명으로 사용자 찾기
    Optional<User> findByUsername(String username);

    // Query Method - 이메일 존재 여부 확인
    boolean existsByEmail(String email);

    // Query Method - 사용자명 존재 여부 확인
    boolean existsByUsername(String username);

    // Query Method - 역할별 사용자 조회
    List<User> findByRole(UserRole role);

    // Query Method - 활성화된 사용자만 조회
    List<User> findByEnabledTrue();

    // Query Method - 비활성화된 사용자만 조회
    List<User> findByEnabledFalse();

    // JPQL - 이메일로 사용자와 RefreshToken 함께 조회
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.refreshTokens WHERE u.email = :email")
    Optional<User> findByEmailWithRefreshTokens(@Param("email") String email);

    // JPQL - ID로 사용자와 RefreshToken 함께 조회
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.refreshTokens WHERE u.id = :id")
    Optional<User> findByIdWithRefreshTokens(@Param("id") Long id);

    // JPQL - 특정 기간 동안 생성된 사용자 조회
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findUsersCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    // Native Query - 역할별 사용자 수 통계
    @Query(value = "SELECT role, COUNT(*) FROM users GROUP BY role", nativeQuery = true)
    List<Object[]> countUsersByRole();

    // Modifying - 사용자 활성화
    @Modifying
    @Query("UPDATE User u SET u.enabled = true WHERE u.id = :id")
    void enableUser(@Param("id") Long id);

    // Modifying - 사용자 비활성화
    @Modifying
    @Query("UPDATE User u SET u.enabled = false WHERE u.id = :id")
    void disableUser(@Param("id") Long id);

    // Modifying - 비밀번호 변경
    @Modifying
    @Query("UPDATE User u SET u.password = :password, u.updatedAt = :updatedAt WHERE u.id = :id")
    void updatePassword(@Param("id") Long id,
                       @Param("password") String password,
                       @Param("updatedAt") LocalDateTime updatedAt);

    // Modifying - 역할 변경
    @Modifying
    @Query("UPDATE User u SET u.role = :role WHERE u.id = :id")
    void updateRole(@Param("id") Long id, @Param("role") UserRole role);

    // 검색 조건 - 이메일 또는 사용자명으로 검색
    @Query("SELECT u FROM User u WHERE u.email LIKE %:keyword% OR u.username LIKE %:keyword%")
    List<User> searchByEmailOrUsername(@Param("keyword") String keyword);

    // 마지막 로그인 시간 기준 비활성 사용자 찾기 (추후 last_login 컬럼 추가 시 사용)
    // @Query("SELECT u FROM User u WHERE u.lastLogin < :date")
    // List<User> findInactiveUsers(@Param("date") LocalDateTime date);
}
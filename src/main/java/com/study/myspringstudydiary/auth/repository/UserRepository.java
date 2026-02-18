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
 * User JPA Repository
 * Spring Data JPA를 사용한 사용자 데이터 접근 인터페이스
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ========== 기본 Query Method ==========

    /**
     * 사용자명으로 사용자 조회
     */
    Optional<User> findByUsername(String username);

    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);

    /**
     * 사용자명 존재 여부 확인
     */
    boolean existsByUsername(String username);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);

    /**
     * 역할로 사용자 목록 조회
     */
    List<User> findByRole(UserRole role);

    /**
     * 활성화된 사용자 목록 조회
     */
    List<User> findByEnabled(boolean enabled);

    /**
     * 특정 날짜 이후 가입한 사용자 조회
     */
    List<User> findByCreatedAtAfter(LocalDateTime date);

    /**
     * 사용자명 또는 이메일로 사용자 조회
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    // ========== @Query를 사용한 커스텀 쿼리 ==========

    /**
     * 역할별 사용자 수 조회
     */
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> countUsersByRole();

    /**
     * 최근 가입한 사용자 조회 (상위 N명)
     */
    @Query("SELECT u FROM User u ORDER BY u.createdAt DESC LIMIT :limit")
    List<User> findRecentUsers(@Param("limit") int limit);

    /**
     * 특정 기간 동안 가입한 사용자 수 조회
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    Long countUsersRegisteredBetween(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * 사용자 활성화 상태 변경
     */
    @Modifying
    @Query("UPDATE User u SET u.enabled = :enabled WHERE u.id = :userId")
    int updateUserEnabledStatus(@Param("userId") Long userId, @Param("enabled") boolean enabled);

    /**
     * 사용자 역할 변경
     */
    @Modifying
    @Query("UPDATE User u SET u.role = :role WHERE u.id = :userId")
    int updateUserRole(@Param("userId") Long userId, @Param("role") UserRole role);

    /**
     * 비밀번호 변경
     */
    @Modifying
    @Query("UPDATE User u SET u.password = :password, u.updatedAt = :updatedAt WHERE u.id = :userId")
    int updatePassword(@Param("userId") Long userId,
                      @Param("password") String password,
                      @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 키워드로 사용자 검색 (사용자명 또는 이메일)
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.email LIKE %:keyword%")
    List<User> searchUsers(@Param("keyword") String keyword);

    /**
     * 최근 활동한 사용자 조회 (업데이트 기준)
     */
    @Query("SELECT u FROM User u WHERE u.updatedAt IS NOT NULL ORDER BY u.updatedAt DESC")
    List<User> findRecentlyActiveUsers();
}
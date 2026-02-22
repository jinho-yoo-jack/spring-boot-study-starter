package com.study.myspringstudydiary.auth.repository;

import com.study.myspringstudydiary.auth.entity.RefreshToken;
import com.study.myspringstudydiary.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * RefreshToken Repository Interface
 * Spring Data JPA repository for RefreshToken entity
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // Query Method - 토큰으로 RefreshToken 찾기
    Optional<RefreshToken> findByToken(String token);

    // Query Method - 사용자로 모든 RefreshToken 찾기
    List<RefreshToken> findByUser(User user);

    // Query Method - 사용자 ID로 모든 RefreshToken 찾기
    List<RefreshToken> findByUserId(Long userId);

    // Query Method - 토큰 존재 여부 확인
    boolean existsByToken(String token);

    // Query Method - 만료되지 않은 토큰 찾기
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.token = :token AND rt.expiresAt > :now")
    Optional<RefreshToken> findByTokenAndNotExpired(@Param("token") String token,
                                                    @Param("now") LocalDateTime now);

    // JPQL - 토큰으로 RefreshToken과 User 함께 조회
    @Query("SELECT rt FROM RefreshToken rt JOIN FETCH rt.user WHERE rt.token = :token")
    Optional<RefreshToken> findByTokenWithUser(@Param("token") String token);

    // JPQL - 유효한 토큰으로 RefreshToken과 User 함께 조회
    @Query("SELECT rt FROM RefreshToken rt JOIN FETCH rt.user WHERE rt.token = :token AND rt.expiresAt > :now")
    Optional<RefreshToken> findValidTokenWithUser(@Param("token") String token,
                                                  @Param("now") LocalDateTime now);

    // JPQL - 사용자의 모든 유효한 토큰 조회
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.expiresAt > :now")
    List<RefreshToken> findValidTokensByUserId(@Param("userId") Long userId,
                                               @Param("now") LocalDateTime now);

    // Modifying - 사용자의 모든 RefreshToken 삭제
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    // Modifying - 만료된 토큰 모두 삭제
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt <= :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    // Modifying - 특정 토큰 삭제
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.token = :token")
    void deleteByToken(@Param("token") String token);

    // Native Query - 사용자별 토큰 개수 통계
    @Query(value = "SELECT user_id, COUNT(*) FROM refresh_tokens GROUP BY user_id", nativeQuery = true)
    List<Object[]> countTokensByUser();

    // Native Query - 특정 IP에서 생성된 토큰 조회
    @Query(value = "SELECT * FROM refresh_tokens WHERE ip_address = ?1", nativeQuery = true)
    List<RefreshToken> findByIpAddress(String ipAddress);

    // 특정 기간 동안 생성된 토큰 조회
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.createdAt BETWEEN :startDate AND :endDate")
    List<RefreshToken> findTokensCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    // 특정 디바이스의 토큰 조회
    List<RefreshToken> findByDeviceInfoContaining(String deviceInfo);

    // 사용자의 가장 최근 토큰 조회
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId ORDER BY rt.createdAt DESC LIMIT 1")
    Optional<RefreshToken> findLatestTokenByUserId(@Param("userId") Long userId);

    // 만료 임박한 토큰 조회 (갱신 대상)
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiresAt BETWEEN :now AND :threshold")
    List<RefreshToken> findTokensExpiringSoon(@Param("now") LocalDateTime now,
                                              @Param("threshold") LocalDateTime threshold);
}
package com.study.myspringstudydiary.global.security.util;

import com.study.myspringstudydiary.global.security.principal.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Security Utility Class
 * Spring Security 컨텍스트에서 인증 정보를 가져오는 유틸리티 클래스
 *
 * 어디서든 현재 로그인한 사용자 정보에 접근할 수 있도록 제공
 */
@Slf4j
public class SecurityUtil {

    private SecurityUtil() {
        // Utility class, prevent instantiation
    }

    /**
     * 현재 인증된 사용자 ID 가져오기
     *
     * @return 사용자 ID (Optional)
     */
    public static Optional<Long> getCurrentUserId() {
        return getCurrentUserPrincipal()
                .map(UserPrincipal::getId);
    }

    /**
     * 현재 인증된 사용자명 가져오기
     *
     * @return 사용자명 (Optional)
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentUserPrincipal()
                .map(UserPrincipal::getUsername);
    }

    /**
     * 현재 인증된 사용자 이메일 가져오기
     *
     * @return 이메일 (Optional)
     */
    public static Optional<String> getCurrentUserEmail() {
        return getCurrentUserPrincipal()
                .map(UserPrincipal::getEmail);
    }

    /**
     * 현재 인증된 UserPrincipal 가져오기
     *
     * @return UserPrincipal (Optional)
     */
    public static Optional<UserPrincipal> getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            log.debug("Security Context에 인증 정보가 없습니다.");
            return Optional.empty();
        }

        if (authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            log.debug("Current user: {}", userPrincipal.getUsername());
            return Optional.of(userPrincipal);
        }

        log.debug("Principal is not UserPrincipal type: {}", authentication.getPrincipal().getClass());
        return Optional.empty();
    }

    /**
     * 현재 인증된 사용자가 있는지 확인
     *
     * @return 인증 여부
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null &&
               authentication.isAuthenticated() &&
               authentication.getPrincipal() instanceof UserPrincipal;
    }

    /**
     * 현재 사용자 ID 가져오기 (없으면 예외 발생)
     *
     * @return 사용자 ID
     * @throws IllegalStateException 인증되지 않은 경우
     */
    public static Long getCurrentUserIdOrThrow() {
        return getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("인증되지 않은 사용자입니다."));
    }

    /**
     * 현재 사용자가 특정 사용자인지 확인
     *
     * @param userId 확인할 사용자 ID
     * @return 일치 여부
     */
    public static boolean isCurrentUser(Long userId) {
        return getCurrentUserId()
                .map(currentId -> currentId.equals(userId))
                .orElse(false);
    }
}
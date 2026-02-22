package com.study.myspringstudydiary.global.security.annotation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

/**
 * Custom annotation to inject currently authenticated user
 * 현재 인증된 사용자 정보를 주입받기 위한 커스텀 어노테이션
 *
 * @AuthenticationPrincipal을 래핑하여 더 간단하고 명확한 사용을 제공
 *
 * Usage:
 * @GetMapping("/me")
 * public User getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
 *     return userPrincipal.getUser();
 * }
 */
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUser {
}
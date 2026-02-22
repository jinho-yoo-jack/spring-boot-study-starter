package com.study.myspringstudydiary.auth.service;

import com.study.myspringstudydiary.auth.entity.User;
import com.study.myspringstudydiary.auth.repository.UserRepository;
import com.study.myspringstudydiary.global.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Custom UserDetailsService implementation with JPA
 * Loads user details from database for Spring Security authentication
 */
@Slf4j
@Primary
@Service("customUserDetailsJpaService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsJpaService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        // Try to find by username first, then by email
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByEmail(username)
                        .orElseThrow(() -> {
                            log.error("User not found: {}", username);
                            return new UsernameNotFoundException("User not found: " + username);
                        }));

        log.debug("User found: {}, role: {}, enabled: {}", user.getUsername(), user.getRole(), user.isEnabled());

        // UserPrincipal 생성 및 반환
        return UserPrincipal.create(user);
    }

    /**
     * Load user by email (additional method for flexibility)
     */
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        // UserPrincipal 생성 및 반환
        return UserPrincipal.create(user);
    }

    /**
     * Check if user exists
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username) || userRepository.existsByEmail(username);
    }
}
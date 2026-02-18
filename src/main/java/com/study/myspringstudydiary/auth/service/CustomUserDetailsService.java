package com.study.myspringstudydiary.auth.service;

import com.study.myspringstudydiary.auth.repository.UserRepository;
import com.study.myspringstudydiary.auth.entity.User;
import com.study.myspringstudydiary.auth.entity.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Custom UserDetailsService implementation with JPA Repository
 * Loads user-specific data from the database
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 읽기 전용 트랜잭션
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        // Try to find user by username first, then by email
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException(
                                "User not found with username or email: " + username)));

        log.debug("User found: {}", user.getUsername());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(getAuthorities(user.getRole()))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.isEnabled())
                .build();
    }

    /**
     * Convert user role to GrantedAuthority collection
     */
    private Collection<? extends GrantedAuthority> getAuthorities(UserRole role) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Convert UserRole enum to Spring Security authority
        if (role != null) {
            String roleStr = "ROLE_" + role.name();
            authorities.add(new SimpleGrantedAuthority(roleStr));
        } else {
            // Default role
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return authorities;
    }
}

package com.study.myspringstudydiary.global.security.principal;

import com.study.myspringstudydiary.auth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * UserPrincipal - Spring Security의 UserDetails 구현체
 *
 * 인증된 사용자의 정보를 담는 클래스
 * Spring Security 컨텍스트에서 사용자 정보에 접근할 때 사용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPrincipal implements UserDetails {

    private Long id;
    private String email;
    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean enabled;

    /**
     * User 엔티티로부터 UserPrincipal 생성
     *
     * @param user User 엔티티
     * @return UserPrincipal 인스턴스
     */
    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        return UserPrincipal.builder()
            .id(user.getId())
            .email(user.getEmail())
            .username(user.getUsername())
            .password(user.getPassword())
            .authorities(authorities)
            .enabled(user.isEnabled())
            .build();
    }

    // UserDetails 인터페이스 구현

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 사용자 ID 조회
     * Controller에서 쉽게 사용자 ID를 가져올 수 있도록 제공
     */
    public Long getUserId() {
        return id;
    }

    /**
     * 사용자 이메일 조회
     */
    public String getEmail() {
        return email;
    }
}
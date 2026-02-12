package com.study.myspringstudydiary.auth.entity;

import lombok.*;
import java.time.LocalDateTime;

/**
 * User Entity for authentication
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "password")
@EqualsAndHashCode(of = "id")
public class User {

    private Long id;
    private String email;
    private String password;
    private String username;
    private UserRole role;

    @Builder.Default
    private boolean enabled = true;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
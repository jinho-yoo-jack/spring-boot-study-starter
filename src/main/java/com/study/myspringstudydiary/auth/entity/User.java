package com.study.myspringstudydiary.auth.entity;

import com.study.myspringstudydiary.study_log.entity.StudyLog;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User Entity with JPA for authentication
 */
@Entity
@Table(name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "username")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"password", "refreshTokens", "studyLogs"})
@EqualsAndHashCode(of = "id")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    // 1:N 양방향 관계 - User에서 StudyLog 목록 참조
    @OneToMany(
        mappedBy = "user",           // StudyLog의 user 필드가 관계의 주인
        cascade = CascadeType.ALL,   // 영속성 전이
        orphanRemoval = true,        // 고아 객체 제거
        fetch = FetchType.LAZY       // 지연 로딩
    )
    @Builder.Default
    private List<StudyLog> studyLogs = new ArrayList<>();

    @Version
    private Long version;

    // 연관관계 편의 메서드
    public void addRefreshToken(RefreshToken refreshToken) {
        refreshTokens.add(refreshToken);
        refreshToken.setUser(this);
    }

    public void removeRefreshToken(RefreshToken refreshToken) {
        refreshTokens.remove(refreshToken);
        refreshToken.setUser(null);
    }

    public void removeAllRefreshTokens() {
        refreshTokens.clear();
    }

    // 연관관계 편의 메서드 - StudyLog 관리
    public void addStudyLog(StudyLog studyLog) {
        studyLogs.add(studyLog);
        studyLog.setUser(this);
    }

    public void removeStudyLog(StudyLog studyLog) {
        studyLogs.remove(studyLog);
        studyLog.setUser(null);
    }

    public void clearStudyLogs() {
        for (StudyLog studyLog : new ArrayList<>(studyLogs)) {
            removeStudyLog(studyLog);
        }
    }
}
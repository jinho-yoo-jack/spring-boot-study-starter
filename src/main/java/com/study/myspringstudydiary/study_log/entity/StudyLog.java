package com.study.myspringstudydiary.study_log.entity;

import com.study.myspringstudydiary.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Study Log Entity with JPA
 */
@Entity
@Table(name = "study_logs",
    indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_category", columnList = "category"),
        @Index(name = "idx_study_date", columnList = "study_date")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"content", "user"})  // content can be large and user to avoid circular reference
@EqualsAndHashCode(of = "id")  // Use only id for equals and hashCode
public class StudyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // N:1 단방향 관계 - StudyLog에서 User를 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Understanding understanding;

    @Column(name = "study_time", nullable = false)
    private Integer studyTime;

    @Column(name = "study_date", nullable = false)
    private LocalDate studyDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    /**
     * Update study log information
     * Only updates non-null values (Partial Update)
     */
    public void update(String title, String content, Category category,
                       Understanding understanding, Integer studyTime, LocalDate studyDate) {

        if (title != null) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
        if (category != null) {
            this.category = category;
        }
        if (understanding != null) {
            this.understanding = understanding;
        }
        if (studyTime != null) {
            this.studyTime = studyTime;
        }
        if (studyDate != null) {
            this.studyDate = studyDate;
        }

        // Update modification time
        this.updatedAt = LocalDateTime.now();
    }

    // Individual update methods for MapStruct
    public void updateTitle(String title) {
        this.title = title;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateCategory(Category category) {
        this.category = category;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateUnderstanding(Understanding understanding) {
        this.understanding = understanding;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStudyTime(Integer studyTime) {
        this.studyTime = studyTime;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStudyDate(LocalDate studyDate) {
        this.studyDate = studyDate;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 연관 관계 편의 메서드 - User 변경
     * 양방향 관계가 설정되면 이 메서드를 통해 동기화
     */
    public void changeUser(User user) {
        this.user = user;
    }
}
package com.study.myspringstudydiary.study_log.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * StudyLog JPA Entity
 * 학습 기록을 저장하는 엔티티
 */
@Entity  // JPA 엔티티 선언
@Table(name = "study_logs")  // 테이블 이름 지정
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "content")  // content can be large, so exclude from toString
@EqualsAndHashCode(of = "id")  // Use only id for equals and hashCode
public class StudyLog {

    @Id  // 기본 키 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // AUTO_INCREMENT
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)  // Enum을 문자열로 저장
    @Column(nullable = false, length = 20)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Understanding understanding;

    @Column(name = "study_time", nullable = false)
    private Integer studyTime;

    @Column(name = "study_date")
    private LocalDate studyDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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

    // ========== JPA 생명주기 콜백 ==========

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (studyDate == null) {
            studyDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
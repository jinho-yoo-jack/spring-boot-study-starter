package com.study.myspringstudydiary.entity;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Study Log Entity with Lombok
 *
 * Before: 90 lines
 * After: 45 lines (50% reduction)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "content")  // content can be large, so exclude from toString
@EqualsAndHashCode(of = "id")  // Use only id for equals and hashCode
public class StudyLog {

    private Long id;
    private String title;
    private String content;
    private Category category;
    private Understanding understanding;
    private Integer studyTime;
    private LocalDate studyDate;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

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
}
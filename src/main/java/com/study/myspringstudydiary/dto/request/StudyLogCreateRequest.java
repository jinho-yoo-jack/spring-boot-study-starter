package com.study.myspringstudydiary.dto.request;

import lombok.*;
import java.time.LocalDate;
import com.study.myspringstudydiary.entity.StudyLog;
import com.study.myspringstudydiary.entity.Category;
import com.study.myspringstudydiary.entity.Understanding;

/**
 * Study Log Create Request DTO with Lombok
 *
 * Before: 33 lines
 * After: 28 lines
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class StudyLogCreateRequest {

    private String title;
    private String content;
    private String category;
    private String understanding;
    private Integer studyTime;
    private LocalDate studyDate;

    /**
     * Convert to Entity
     */
    public StudyLog toEntity() {
        return StudyLog.builder()
                .title(this.title)
                .content(this.content)
                .category(Category.valueOf(this.category))
                .understanding(Understanding.valueOf(this.understanding))
                .studyTime(this.studyTime)
                .studyDate(this.studyDate != null ? this.studyDate : LocalDate.now())
                .build();
    }
}
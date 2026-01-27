package com.study.myspringstudydiary.dto.response;

import lombok.*;
import com.study.myspringstudydiary.entity.StudyLog;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Study Log Response DTO with Lombok
 * Using @Value for immutable response
 *
 * Before: 54 lines
 * After: 35 lines (35% reduction)
 */
@Value
@Builder
public class StudyLogResponse {

    Long id;
    String title;
    String content;
    String category;
    String categoryIcon;
    String understanding;
    String understandingEmoji;
    Integer studyTime;
    LocalDate studyDate;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    /**
     * Convert Entity to Response DTO
     * Using static factory method with Builder pattern
     */
    public static StudyLogResponse from(StudyLog studyLog) {
        return StudyLogResponse.builder()
                .id(studyLog.getId())
                .title(studyLog.getTitle())
                .content(studyLog.getContent())
                .category(studyLog.getCategory().name())
                .categoryIcon(studyLog.getCategory().getIcon())
                .understanding(studyLog.getUnderstanding().name())
                .understandingEmoji(studyLog.getUnderstanding().getEmoji())
                .studyTime(studyLog.getStudyTime())
                .studyDate(studyLog.getStudyDate())
                .createdAt(studyLog.getCreatedAt())
                .updatedAt(studyLog.getUpdatedAt())
                .build();
    }
}
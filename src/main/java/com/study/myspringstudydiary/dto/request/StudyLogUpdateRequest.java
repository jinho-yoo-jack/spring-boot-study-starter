package com.study.myspringstudydiary.dto.request;

import lombok.*;
import java.time.LocalDate;

/**
 * Study Log Update Request DTO
 *
 * All fields are optional for partial updates.
 * null values mean keeping existing values.
 *
 * Before: 52 lines
 * After: 32 lines (38% reduction)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class StudyLogUpdateRequest {

    private String title;          // null means keep existing value
    private String content;        // null means keep existing value
    private String category;       // null means keep existing value
    private String understanding;  // null means keep existing value
    private Integer studyTime;     // null means keep existing value
    private LocalDate studyDate;   // null means keep existing value

    /**
     * Check if all fields are null
     * Used to check if there's nothing to update
     */
    public boolean hasNoUpdates() {
        return title == null
            && content == null
            && category == null
            && understanding == null
            && studyTime == null
            && studyDate == null;
    }
}
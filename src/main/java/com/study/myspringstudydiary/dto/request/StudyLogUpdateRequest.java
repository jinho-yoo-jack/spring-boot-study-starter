package com.study.myspringstudydiary.dto.request;

import lombok.*;
import java.time.LocalDate;
import com.study.myspringstudydiary.entity.Category;
import com.study.myspringstudydiary.entity.Understanding;
import com.study.myspringstudydiary.validation.ValidEnum;
import jakarta.validation.constraints.*;

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

    @Size(max = 100, message = "학습 주제는 100자를 초과할 수 없습니다")
    private String title;          // null means keep existing value

    @Size(max = 1000, message = "학습 내용은 1000자를 초과할 수 없습니다")
    private String content;        // null means keep existing value

    @ValidEnum(enumClass = Category.class, message = "유효하지 않은 카테고리입니다", nullable = true)
    private String category;       // null means keep existing value

    @ValidEnum(enumClass = Understanding.class, message = "유효하지 않은 이해도입니다", nullable = true)
    private String understanding;  // null means keep existing value

    @Min(value = 1, message = "학습 시간은 1분 이상이어야 합니다")
    @Max(value = 1440, message = "학습 시간은 1440분을 초과할 수 없습니다")
    private Integer studyTime;     // null means keep existing value

    @PastOrPresent(message = "학습 날짜는 미래일 수 없습니다")
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
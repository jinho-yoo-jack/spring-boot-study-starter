package com.study.myspringstudydiary.study_log.dto.request;

import lombok.*;
import java.time.LocalDate;
import jakarta.validation.constraints.*;

/**
 * Study Log Update Request DTO with Validation
 *
 * 학습 일지 수정 요청 DTO
 * All fields are optional for partial updates.
 * null values mean keeping existing values.
 * 값이 있을 경우에만 검증 수행
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class StudyLogUpdateRequest {

    @Size(min = 2, max = 100, message = "제목은 2자 이상 100자 이하여야 합니다")
    private String title;          // null means keep existing value

    @Size(min = 10, max = 5000, message = "내용은 10자 이상 5000자 이하여야 합니다")
    private String content;        // null means keep existing value

    @Pattern(regexp = "^(JAVA|SPRING|JPA|DATABASE|ALGORITHM|CS|NETWORK|GIT|ETC)$",
             message = "카테고리는 JAVA, SPRING, JPA, DATABASE, ALGORITHM, CS, NETWORK, GIT, ETC 중 하나여야 합니다")
    private String category;       // null means keep existing value

    @Pattern(regexp = "^(VERY_GOOD|GOOD|NORMAL|BAD|VERY_BAD)$",
             message = "이해도는 VERY_GOOD, GOOD, NORMAL, BAD, VERY_BAD 중 하나여야 합니다")
    private String understanding;  // null means keep existing value

    @Positive(message = "학습 시간은 양수여야 합니다")
    @Max(value = 1440, message = "학습 시간은 1440분(24시간)을 초과할 수 없습니다")
    private Integer studyTime;     // null means keep existing value

    @PastOrPresent(message = "학습 날짜는 현재 또는 과거여야 합니다")
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
package com.study.myspringstudydiary.dto.request;

import lombok.*;
import java.time.LocalDate;
import com.study.myspringstudydiary.entity.StudyLog;
import com.study.myspringstudydiary.entity.Category;
import com.study.myspringstudydiary.entity.Understanding;
import jakarta.validation.constraints.*;

/**
 * Study Log Create Request DTO with Lombok and Validation
 *
 * 학습 일지 생성 요청 DTO
 * Bean Validation을 통해 입력값을 검증
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class StudyLogCreateRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(min = 2, max = 100, message = "제목은 2자 이상 100자 이하여야 합니다")
    private String title;

    @NotBlank(message = "내용은 필수입니다")
    @Size(min = 10, max = 5000, message = "내용은 10자 이상 5000자 이하여야 합니다")
    private String content;

    @NotBlank(message = "카테고리는 필수입니다")
    @Pattern(regexp = "^(JAVA|SPRING|JPA|DATABASE|ALGORITHM|CS|NETWORK|GIT|ETC)$",
             message = "카테고리는 JAVA, SPRING, JPA, DATABASE, ALGORITHM, CS, NETWORK, GIT, ETC 중 하나여야 합니다")
    private String category;

    @NotBlank(message = "이해도는 필수입니다")
    @Pattern(regexp = "^(VERY_GOOD|GOOD|NORMAL|BAD|VERY_BAD)$",
             message = "이해도는 VERY_GOOD, GOOD, NORMAL, BAD, VERY_BAD 중 하나여야 합니다")
    private String understanding;

    @NotNull(message = "학습 시간은 필수입니다")
    @Positive(message = "학습 시간은 양수여야 합니다")
    @Max(value = 1440, message = "학습 시간은 1440분(24시간)을 초과할 수 없습니다")
    private Integer studyTime;

    @PastOrPresent(message = "학습 날짜는 현재 또는 과거여야 합니다")
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
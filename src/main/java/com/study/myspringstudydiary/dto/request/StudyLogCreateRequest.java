package com.study.myspringstudydiary.dto.request;

import lombok.*;
import java.time.LocalDate;
import com.study.myspringstudydiary.entity.StudyLog;
import com.study.myspringstudydiary.entity.Category;
import com.study.myspringstudydiary.entity.Understanding;
import com.study.myspringstudydiary.validation.ValidEnum;
import jakarta.validation.constraints.*;

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

    @NotBlank(message = "학습 주제는 필수입니다")
    @Size(max = 100, message = "학습 주제는 100자를 초과할 수 없습니다")
    private String title;

    @NotBlank(message = "학습 내용은 필수입니다")
    @Size(max = 1000, message = "학습 내용은 1000자를 초과할 수 없습니다")
    private String content;

    @NotBlank(message = "카테고리는 필수입니다")
    @ValidEnum(enumClass = Category.class, message = "유효하지 않은 카테고리입니다", nullable = false)
    private String category;

    @NotBlank(message = "이해도는 필수입니다")
    @ValidEnum(enumClass = Understanding.class, message = "유효하지 않은 이해도입니다", nullable = false)
    private String understanding;

    @NotNull(message = "학습 시간은 필수입니다")
    @Min(value = 1, message = "학습 시간은 1분 이상이어야 합니다")
    @Max(value = 1440, message = "학습 시간은 1440분을 초과할 수 없습니다")
    private Integer studyTime;

    @PastOrPresent(message = "학습 날짜는 미래일 수 없습니다")
    private LocalDate studyDate;

    /**
     * Convert to Entity
     * @param userId ID of the user creating this study log
     */
    public StudyLog toEntity(Long userId) {
        return StudyLog.builder()
                .userId(userId)
                .title(this.title)
                .content(this.content)
                .category(Category.valueOf(this.category))
                .understanding(Understanding.valueOf(this.understanding))
                .studyTime(this.studyTime)
                .studyDate(this.studyDate != null ? this.studyDate : LocalDate.now())
                .build();
    }
}
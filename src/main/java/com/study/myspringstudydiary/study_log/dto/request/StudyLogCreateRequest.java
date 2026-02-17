package com.study.myspringstudydiary.study_log.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDate;
import com.study.myspringstudydiary.study_log.entity.StudyLog;
import com.study.myspringstudydiary.study_log.entity.Category;
import com.study.myspringstudydiary.study_log.entity.Understanding;
import jakarta.validation.constraints.*;

/**
 * Study Log Create Request DTO with Lombok and Validation
 *
 * 학습 일지 생성 요청 DTO
 * Bean Validation을 통해 입력값을 검증
 */
@Schema(description = "학습 기록 생성 요청 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class StudyLogCreateRequest {

    @Schema(description = "학습 제목", example = "Spring Security JWT 인증 구현", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "제목은 필수입니다")
    @Size(min = 2, max = 100, message = "제목은 2자 이상 100자 이하여야 합니다")
    private String title;

    @Schema(description = "학습 내용", example = "JWT 토큰 생성 및 검증 로직을 구현하고 Spring Security와 통합했습니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "내용은 필수입니다")
    @Size(min = 10, max = 5000, message = "내용은 10자 이상 5000자 이하여야 합니다")
    private String content;

    @Schema(description = "카테고리", example = "SPRING", allowableValues = {"JAVA", "SPRING", "JPA", "DATABASE", "ALGORITHM", "CS", "NETWORK", "GIT", "ETC"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "카테고리는 필수입니다")
    @Pattern(regexp = "^(JAVA|SPRING|JPA|DATABASE|ALGORITHM|CS|NETWORK|GIT|ETC)$",
             message = "카테고리는 JAVA, SPRING, JPA, DATABASE, ALGORITHM, CS, NETWORK, GIT, ETC 중 하나여야 합니다")
    private String category;

    @Schema(description = "이해도", example = "GOOD", allowableValues = {"VERY_GOOD", "GOOD", "NORMAL", "BAD", "VERY_BAD"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이해도는 필수입니다")
    @Pattern(regexp = "^(VERY_GOOD|GOOD|NORMAL|BAD|VERY_BAD)$",
             message = "이해도는 VERY_GOOD, GOOD, NORMAL, BAD, VERY_BAD 중 하나여야 합니다")
    private String understanding;

    @Schema(description = "학습 시간 (분 단위)", example = "120", minimum = "1", maximum = "1440", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "학습 시간은 필수입니다")
    @Positive(message = "학습 시간은 양수여야 합니다")
    @Max(value = 1440, message = "학습 시간은 1440분(24시간)을 초과할 수 없습니다")
    private Integer studyTime;

    @Schema(description = "학습 날짜 (생략 시 오늘 날짜)", example = "2024-01-15", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
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
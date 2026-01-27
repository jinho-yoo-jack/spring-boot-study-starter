package com.study.myspringstudydiary.dto.response;

import lombok.*;

/**
 * Study Log Delete Response DTO with Lombok
 * Using @Value for immutable response
 *
 * Before: 28 lines
 * After: 18 lines (36% reduction)
 */
@Value
@Builder
public class StudyLogDeleteResponse {

    @Builder.Default
    String message = "학습 일지가 성공적으로 삭제되었습니다.";
    Long deletedId;

    /**
     * Static factory method
     */
    public static StudyLogDeleteResponse of(Long id) {
        return StudyLogDeleteResponse.builder()
                .deletedId(id)
                .build();
    }
}
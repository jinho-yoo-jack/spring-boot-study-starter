package com.study.myspringstudydiary.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.study.myspringstudydiary.model.Diary;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DiaryResponse {

    private Long id;

    private String title;

    private String content;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public static DiaryResponse from(Diary diary) {
        return DiaryResponse.builder()
                .id(diary.getId())
                .title(diary.getTitle())
                .content(diary.getContent())
                .userId(diary.getUserId())
                .userName(diary.getUserName())
                .createdAt(diary.getCreatedAt())
                .updatedAt(diary.getUpdatedAt())
                .build();
    }
}
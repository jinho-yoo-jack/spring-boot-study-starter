package com.study.myspringstudydiary.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryRequest {

    @NotNull(message = "Title must not be null")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;

    @NotNull(message = "Content must not be null")
    @Size(min = 10, message = "Content must be at least 10 characters")
    private String content;

    @JsonProperty("user_id")
    private Long userId;
}
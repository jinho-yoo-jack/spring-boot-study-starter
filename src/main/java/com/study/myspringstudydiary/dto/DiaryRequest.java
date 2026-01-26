package com.study.myspringstudydiary.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryRequest {

    private String title;

    private String content;

    @JsonProperty("user_id")
    private Long userId;
}
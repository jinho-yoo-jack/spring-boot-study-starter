package com.study.myspringstudydiary.study_log.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Understanding {
    VERY_GOOD("😎", "완벽히 이해했어요"),
    GOOD("😊", "잘 이해했어요"),
    NORMAL("😐", "보통이에요"),
    BAD("😥", "어려웠어요"),
    VERY_BAD("😵", "이해 못했어요");

    private final String emoji;
    private final String description;
}
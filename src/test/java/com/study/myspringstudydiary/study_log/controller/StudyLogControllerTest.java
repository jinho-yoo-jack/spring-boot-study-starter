package com.study.myspringstudydiary.study_log.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.myspringstudydiary.study_log.dto.request.StudyLogCreateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class StudyLogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("인증되지 않은 사용자는 학습 기록을 생성할 수 없다")
    void createStudyLog_unauthorized() throws Exception {
        // Given
        StudyLogCreateRequest request = StudyLogCreateRequest.builder()
                .title("Spring Security 학습")
                .content("JWT 토큰 구현 완료")
                .category("SPRING")
                .understanding("GOOD")
                .studyTime(120)
                .build();

        // When & Then
        // TODO: 인증 헤더 없이 POST /api/v1/logs 요청 시 401 반환 검증
        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("인증된 사용자는 학습 기록을 생성할 수 있다")
    @WithMockUser(username = "testuser", roles = "USER")
    void createStudyLog_success() throws Exception {
        // Given
        StudyLogCreateRequest request = StudyLogCreateRequest.builder()
                .title("Spring Security 학습")
                .content("JWT 토큰 구현 완료")
                .category("SPRING")
                .understanding("GOOD")
                .studyTime(120)
                .build();

        // When & Then
        // TODO: POST /api/v1/logs 요청 시 201 Created 반환 검증
        // TODO: 응답 본문에 id, title, content가 포함되어 있는지 검증
    }

    @Test
    @DisplayName("유효하지 않은 데이터로 학습 기록 생성 시 400 에러")
    @WithMockUser(username = "testuser", roles = "USER")
    void createStudyLog_invalidData() throws Exception {
        // Given - title이 없는 잘못된 요청
        String invalidRequest = """
            {
                "content": "내용만 있음",
                "category": "SPRING",
                "understanding": "GOOD",
                "studyTime": 120
            }
            """;

        // When & Then
        // TODO: 400 Bad Request가 반환되는지 검증
        // TODO: 에러 메시지에 "title"이 포함되어 있는지 검증
    }

    @Test
    @DisplayName("자신의 학습 기록 목록을 조회할 수 있다")
    @WithMockUser(username = "testuser", roles = "USER")
    void getStudyLogs_success() throws Exception {
        // TODO: GET /api/v1/logs 요청으로 목록 조회 테스트 작성
    }
}
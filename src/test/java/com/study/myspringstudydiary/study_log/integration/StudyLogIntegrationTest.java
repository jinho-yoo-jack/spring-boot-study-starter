package com.study.myspringstudydiary.study_log.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.myspringstudydiary.study_log.dto.request.StudyLogCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)  // Security 필터 비활성화
@ActiveProfiles("test")
@Transactional
@DisplayName("StudyLog 통합 테스트")
class StudyLogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private StudyLogCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        createRequest = new StudyLogCreateRequest();
        createRequest.setTitle("Spring Boot 학습");
        createRequest.setContent("통합 테스트 작성");
        createRequest.setCategory("SPRING");
        createRequest.setUnderstanding("GOOD");
        createRequest.setStudyTime(120);
        createRequest.setStudyDate(LocalDate.now());
    }

    @Test
    @DisplayName("학습 기록 생성 및 조회 통합 테스트")
    void createAndGetStudyLog() throws Exception {
        // 1. 학습 기록 생성
        String responseContent = mockMvc.perform(
                post("/api/v1/logs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.title").value("Spring Boot 학습"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        // ID 추출
        Long createdId = objectMapper.readTree(responseContent)
            .get("data")
            .get("id")
            .asLong();

        // 2. 생성된 학습 기록 조회
        mockMvc.perform(get("/api/v1/logs/{id}", createdId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(createdId))
            .andExpect(jsonPath("$.data.title").value("Spring Boot 학습"));
    }

    @Test
    @DisplayName("전체 학습 기록 조회")
    void getAllStudyLogs() throws Exception {
        // 학습 기록 생성
        mockMvc.perform(
            post("/api/v1/logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        ).andExpect(status().isCreated());

        // 전체 조회
        mockMvc.perform(get("/api/v1/logs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
    }
}
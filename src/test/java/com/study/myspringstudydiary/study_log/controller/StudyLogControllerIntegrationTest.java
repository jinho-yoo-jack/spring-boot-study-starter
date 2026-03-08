package com.study.myspringstudydiary.study_log.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.myspringstudydiary.global.common.Page;
import com.study.myspringstudydiary.study_log.dto.request.StudyLogCreateRequest;
import com.study.myspringstudydiary.study_log.dto.request.StudyLogUpdateRequest;
import com.study.myspringstudydiary.study_log.dto.response.StudyLogDeleteResponse;
import com.study.myspringstudydiary.study_log.dto.response.StudyLogResponse;
import com.study.myspringstudydiary.study_log.exception.StudyLogNotFoundException;
import com.study.myspringstudydiary.study_log.service.StudyLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // 보안 필터 비활성화
@ActiveProfiles("test")
@DisplayName("StudyLog Controller 통합 테스트")
class StudyLogControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudyLogService studyLogService;

    private StudyLogResponse testResponse;
    private StudyLogCreateRequest createRequest;
    private StudyLogUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testResponse = StudyLogResponse.builder()
                .id(1L)
                .title("Spring Boot 테스트 학습")
                .content("MockMvc를 사용한 컨트롤러 테스트 작성 방법을 학습했습니다.")
                .studyDate(LocalDate.now())
                .category("SPRING")
                .categoryIcon("🍃")
                .understanding("VERY_GOOD")
                .understandingEmoji("😊")
                .studyTime(120)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = StudyLogCreateRequest.builder()
                .title("Spring Boot 테스트 학습")
                .content("MockMvc를 사용한 컨트롤러 테스트 작성 방법을 학습했습니다.")
                .studyDate(LocalDate.now())
                .category("SPRING")
                .understanding("VERY_GOOD")
                .studyTime(120)
                .build();

        updateRequest = StudyLogUpdateRequest.builder()
                .title("Spring Boot 테스트 학습 (수정)")
                .content("MockMvc를 사용한 컨트롤러 테스트 작성 방법을 심화 학습했습니다.")
                .studyDate(LocalDate.now())
                .category("SPRING")
                .understanding("VERY_GOOD")
                .studyTime(180)
                .build();
    }

    @Nested
    @DisplayName("CREATE 작업")
    class CreateOperations {

        @Test
        @DisplayName("학습 일지 생성 - 성공")
        void createStudyLog_Success() throws Exception {
            // Given
            when(studyLogService.createStudyLog(any(StudyLogCreateRequest.class)))
                    .thenReturn(testResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/logs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.title").value("Spring Boot 테스트 학습"));
        }

        @Test
        @DisplayName("학습 일지 생성 - 유효성 검증 실패 (짧은 내용)")
        void createStudyLog_ValidationFailed_ShortContent() throws Exception {
            // Given
            StudyLogCreateRequest invalidRequest = StudyLogCreateRequest.builder()
                    .title("테스트 제목")
                    .content("짧은 내용")  // 10자 미만
                    .studyDate(LocalDate.now())
                    .category("SPRING")
                    .understanding("GOOD")
                    .build();

            // When & Then
            mockMvc.perform(post("/api/v1/logs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("READ 작업")
    class ReadOperations {

        @Test
        @DisplayName("모든 학습 일지 조회 - 성공")
        void getAllStudyLogs_Success() throws Exception {
            // Given
            List<StudyLogResponse> responses = Arrays.asList(testResponse);
            when(studyLogService.getAllStudyLogs()).thenReturn(responses);

            // When & Then
            mockMvc.perform(get("/api/v1/logs"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].title").value("Spring Boot 테스트 학습"));
        }

        @Test
        @DisplayName("ID로 학습 일지 조회 - 성공")
        void getStudyLogById_Success() throws Exception {
            // Given
            when(studyLogService.getStudyLogById(1L)).thenReturn(testResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/logs/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Spring Boot 테스트 학습"));
        }

        @Test
        @DisplayName("ID로 학습 일지 조회 - 존재하지 않음")
        void getStudyLogById_NotFound() throws Exception {
            // Given
            when(studyLogService.getStudyLogById(999L))
                    .thenThrow(new StudyLogNotFoundException(999L));

            // When & Then
            mockMvc.perform(get("/api/v1/logs/999"))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("UPDATE 작업")
    class UpdateOperations {

        @Test
        @DisplayName("학습 일지 수정 - 성공")
        void updateStudyLog_Success() throws Exception {
            // Given
            StudyLogResponse updatedResponse = StudyLogResponse.builder()
                    .id(1L)
                    .title("Spring Boot 테스트 학습 (수정)")
                    .content("MockMvc를 사용한 컨트롤러 테스트 작성 방법을 심화 학습했습니다.")
                    .studyDate(LocalDate.now())
                    .category("SPRING")
                    .categoryIcon("🍃")
                    .understanding("VERY_GOOD")
                    .understandingEmoji("😊")
                    .studyTime(180)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(studyLogService.updateStudyLog(eq(1L), any(StudyLogUpdateRequest.class)))
                    .thenReturn(updatedResponse);

            // When & Then
            mockMvc.perform(put("/api/v1/logs/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Spring Boot 테스트 학습 (수정)"))
                    .andExpect(jsonPath("$.understanding").value("VERY_GOOD"));
        }
    }

    @Nested
    @DisplayName("DELETE 작업")
    class DeleteOperations {

        @Test
        @DisplayName("학습 일지 삭제 - 성공")
        void deleteStudyLog_Success() throws Exception {
            // Given
            StudyLogDeleteResponse deleteResponse = StudyLogDeleteResponse.of(1L);
            when(studyLogService.deleteStudyLog(1L)).thenReturn(deleteResponse);

            // When & Then
            mockMvc.perform(delete("/api/v1/logs/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.deletedId").value(1))
                    .andExpect(jsonPath("$.message").value("학습 일지가 성공적으로 삭제되었습니다."));
        }

        @Test
        @DisplayName("학습 일지 삭제 - 존재하지 않음")
        void deleteStudyLog_NotFound() throws Exception {
            // Given
            when(studyLogService.deleteStudyLog(999L))
                    .thenThrow(new StudyLogNotFoundException(999L));

            // When & Then
            mockMvc.perform(delete("/api/v1/logs/999"))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }
}
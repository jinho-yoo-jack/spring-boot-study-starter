package com.study.myspringstudydiary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.myspringstudydiary.dto.request.StudyLogCreateRequest;
import com.study.myspringstudydiary.dto.request.StudyLogUpdateRequest;
import com.study.myspringstudydiary.dto.response.StudyLogResponse;
import com.study.myspringstudydiary.dto.response.StudyLogDeleteResponse;
import com.study.myspringstudydiary.service.StudyLogService;
import com.study.myspringstudydiary.security.JwtAuthenticationFilter;
import com.study.myspringstudydiary.security.JwtTokenProvider;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(
    controllers = StudyLogController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {JwtAuthenticationFilter.class}
    )
)
@DisplayName("StudyLog Controller 테스트")
class StudyLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudyLogService studyLogService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private StudyLogCreateRequest validRequest;
    private StudyLogResponse mockResponse;

    @BeforeEach
    void setUp() {
        validRequest = new StudyLogCreateRequest();
        validRequest.setTitle("테스트 제목");
        validRequest.setContent("테스트 내용");
        validRequest.setCategory("SPRING");
        validRequest.setUnderstanding("VERY_GOOD");
        validRequest.setStudyTime(120);
        validRequest.setStudyDate(LocalDate.now());

        mockResponse = StudyLogResponse.builder()
            .id(1L)
            .title("테스트 제목")
            .content("테스트 내용")
            .category("SPRING")
            .categoryIcon("🌱")
            .understanding("VERY_GOOD")
            .understandingEmoji("😎")
            .studyTime(120)
            .studyDate(LocalDate.now())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Nested
    @DisplayName("POST /api/v1/logs")
    class CreateStudyLog {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("성공: 학습 기록 생성")
        void createStudyLog_Success() throws Exception {
            // Given
            given(studyLogService.createStudyLog(any(StudyLogCreateRequest.class)))
                .willReturn(mockResponse);

            // When & Then
            mockMvc.perform(
                post("/api/v1/logs")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest))
            )
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.title").value("테스트 제목"))
            .andExpect(jsonPath("$.data.category").value("SPRING"));
        }

        @Test
        @WithMockUser
        @DisplayName("실패: 유효성 검사 실패 - 필수 필드 누락")
        void createStudyLog_ValidationFailed() throws Exception {
            // Given - 필수 필드 누락
            StudyLogCreateRequest invalidRequest = new StudyLogCreateRequest();

            // When & Then
            mockMvc.perform(
                post("/api/v1/logs")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
            )
            .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패: 인증되지 않은 사용자")
        void createStudyLog_Unauthorized() throws Exception {
            // When & Then
            mockMvc.perform(
                post("/api/v1/logs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest))
            )
            .andExpect(status().isForbidden()); // CSRF 토큰이 없으면 403 반환
        }
    }

    @Nested
    @DisplayName("GET /api/v1/logs")
    class GetStudyLogs {

        @Test
        @WithMockUser
        @DisplayName("성공: 모든 학습 기록 조회")
        void getAllStudyLogs_Success() throws Exception {
            // Given
            List<StudyLogResponse> responses = Arrays.asList(mockResponse);
            given(studyLogService.getAllStudyLogs()).willReturn(responses);

            // When & Then
            mockMvc.perform(get("/api/v1/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("테스트 제목"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/logs/{id}")
    class GetStudyLogById {

        @Test
        @WithMockUser
        @DisplayName("성공: ID로 학습 기록 조회")
        void getStudyLogById_Success() throws Exception {
            // Given
            Long studyLogId = 1L;
            given(studyLogService.getStudyLogById(studyLogId)).willReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/logs/{id}", studyLogId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/logs/{id}")
    class UpdateStudyLog {

        @Test
        @WithMockUser
        @DisplayName("성공: 학습 기록 수정")
        void updateStudyLog_Success() throws Exception {
            // Given
            Long studyLogId = 1L;
            StudyLogUpdateRequest updateRequest = new StudyLogUpdateRequest();
            updateRequest.setTitle("수정된 제목");

            given(studyLogService.updateStudyLog(eq(studyLogId), any(StudyLogUpdateRequest.class)))
                .willReturn(mockResponse);

            // When & Then
            mockMvc.perform(
                put("/api/v1/logs/{id}", studyLogId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/logs/{id}")
    class DeleteStudyLog {

        @Test
        @WithMockUser
        @DisplayName("성공: 학습 기록 삭제")
        void deleteStudyLog_Success() throws Exception {
            // Given
            Long studyLogId = 1L;
            StudyLogDeleteResponse deleteResponse = StudyLogDeleteResponse.of(studyLogId);
            given(studyLogService.deleteStudyLog(studyLogId)).willReturn(deleteResponse);

            // When & Then
            mockMvc.perform(
                delete("/api/v1/logs/{id}", studyLogId)
                    .with(csrf())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.deletedId").value(1))
            .andExpect(jsonPath("$.data.message").value("학습 일지가 성공적으로 삭제되었습니다."));
        }
    }
}
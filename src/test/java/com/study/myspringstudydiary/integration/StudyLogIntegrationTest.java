package com.study.myspringstudydiary.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.myspringstudydiary.auth.dto.LoginRequest;
import com.study.myspringstudydiary.auth.dto.LoginResponse;
import com.study.myspringstudydiary.study_log.dto.request.StudyLogCreateRequest;
import com.study.myspringstudydiary.study_log.dto.request.StudyLogUpdateRequest;
import com.study.myspringstudydiary.study_log.entity.Category;
import com.study.myspringstudydiary.study_log.entity.Understanding;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("StudyLog 통합 테스트")
class StudyLogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String accessToken;
    private Long createdStudyLogId;

    @BeforeEach
    void setUp() throws Exception {
        // 테스트용 사용자 생성 및 로그인
        LoginRequest loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(
                objectMapper.readTree(responseBody).get("data").toString(),
                LoginResponse.class
        );

        accessToken = loginResponse.getAccessToken();
    }

    @Test
    @Order(1)
    @DisplayName("1. 학습 일지 생성 - 통합 테스트")
    void createStudyLog_IntegrationTest() throws Exception {
        // Given
        StudyLogCreateRequest request = StudyLogCreateRequest.builder()
                .title("Spring Boot 통합 테스트")
                .content("Spring Boot에서 통합 테스트를 작성하는 방법을 학습했습니다.")
                .studyDate(LocalDate.now())
                .category("SPRING")
                .understanding("GOOD")
                .studyTime(120)
                .build();

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/v1/logs")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Spring Boot 통합 테스트"))
                .andExpect(jsonPath("$.data.understanding").value("GOOD"))
                .andReturn();

        // ID 저장 (다음 테스트에서 사용)
        String responseBody = result.getResponse().getContentAsString();
        createdStudyLogId = objectMapper.readTree(responseBody)
                .get("data").get("id").asLong();
    }

    @Test
    @Order(2)
    @DisplayName("2. 생성된 학습 일지 조회 - 통합 테스트")
    @Transactional
    void getCreatedStudyLog_IntegrationTest() throws Exception {
        // 먼저 학습 일지 생성
        StudyLogCreateRequest createRequest = StudyLogCreateRequest.builder()
                .title("조회 테스트용 학습 일지")
                .content("이 학습 일지는 조회 테스트를 위해 생성되었습니다.")
                .studyDate(LocalDate.now())
                .category("SPRING")
                .understanding("VERY_GOOD")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/logs")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("data").get("id").asLong();

        // When & Then - 생성된 학습 일지 조회
        mockMvc.perform(get("/api/v1/logs/{id}", id)
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("조회 테스트용 학습 일지"))
                .andExpect(jsonPath("$.category").value("SPRING"))
                .andExpect(jsonPath("$.understanding").value("VERY_GOOD"));
    }

    @Test
    @Order(3)
    @DisplayName("3. 학습 일지 수정 - 통합 테스트")
    @Transactional
    void updateStudyLog_IntegrationTest() throws Exception {
        // 먼저 학습 일지 생성
        StudyLogCreateRequest createRequest = StudyLogCreateRequest.builder()
                .title("수정 전 제목")
                .content("수정 전 내용입니다. 이 내용은 곧 수정될 예정입니다.")
                .studyDate(LocalDate.now())
                .category("DATABASE")
                .understanding("NORMAL")
                .studyTime(90)
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/logs")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("data").get("id").asLong();

        // Given - 수정 요청
        StudyLogUpdateRequest updateRequest = StudyLogUpdateRequest.builder()
                .title("수정된 제목")
                .content("수정된 내용입니다. 성공적으로 업데이트되었습니다.")
                .studyDate(LocalDate.now())
                .category("DATABASE")
                .understanding("VERY_GOOD")
                .studyTime(120)
                .build();

        // When & Then
        mockMvc.perform(put("/api/v1/logs/{id}", id)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정된 제목"))
                .andExpect(jsonPath("$.content").value("수정된 내용입니다. 성공적으로 업데이트되었습니다."))
                .andExpect(jsonPath("$.understanding").value("EXCELLENT"));
    }

    @Test
    @Order(4)
    @DisplayName("4. 학습 일지 삭제 - 통합 테스트")
    @Transactional
    void deleteStudyLog_IntegrationTest() throws Exception {
        // 먼저 학습 일지 생성
        StudyLogCreateRequest createRequest = StudyLogCreateRequest.builder()
                .title("삭제될 학습 일지")
                .content("이 학습 일지는 테스트를 위해 곧 삭제될 예정입니다.")
                .studyDate(LocalDate.now())
                .category("ALGORITHM")
                .understanding("BAD")
                .studyTime(60)
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/logs")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("data").get("id").asLong();

        // When & Then - 삭제
        mockMvc.perform(delete("/api/v1/logs/{id}", id)
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.deleted").value(true));

        // 삭제 확인 - 조회 시 404
        mockMvc.perform(get("/api/v1/logs/{id}", id)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(5)
    @DisplayName("5. 페이징 조회 - 통합 테스트")
    void getStudyLogsWithPaging_IntegrationTest() throws Exception {
        // Given - 여러 개의 학습 일지 생성
        for (int i = 1; i <= 3; i++) {
            StudyLogCreateRequest request = StudyLogCreateRequest.builder()
                    .title("페이징 테스트 " + i)
                    .content("페이징 테스트를 위한 학습 일지 " + i + "번째 내용입니다.")
                    .studyDate(LocalDate.now())
                    .category("JAVA")
                    .understanding("GOOD")
                    .studyTime(100)
                    .build();

            mockMvc.perform(post("/api/v1/logs")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // When & Then
        mockMvc.perform(get("/api/v1/logs/page")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("page", "0")
                        .param("size", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(lessThanOrEqualTo(2))))
                .andExpect(jsonPath("$.totalPages").exists())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(2));
    }

    @Test
    @Order(6)
    @DisplayName("6. 카테고리별 조회 - 통합 테스트")
    @Transactional
    void getStudyLogsByCategory_IntegrationTest() throws Exception {
        // Given - SPRING 카테고리 학습 일지 생성
        StudyLogCreateRequest springRequest = StudyLogCreateRequest.builder()
                .title("Spring Security 학습")
                .content("Spring Security의 인증과 인가에 대해 학습했습니다.")
                .studyDate(LocalDate.now())
                .category("SPRING")
                .understanding("VERY_GOOD")
                .build();

        mockMvc.perform(post("/api/v1/logs")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(springRequest)))
                .andExpect(status().isCreated());

        // When & Then
        mockMvc.perform(get("/api/v1/logs/category/SPRING")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].category", everyItem(is("SPRING"))));
    }

    @Test
    @Order(7)
    @DisplayName("7. 검색 기능 - 통합 테스트")
    @Transactional
    void searchStudyLogs_IntegrationTest() throws Exception {
        // Given - 검색 대상 학습 일지 생성
        StudyLogCreateRequest searchTarget = StudyLogCreateRequest.builder()
                .title("JPA 심화 학습")
                .content("JPA의 영속성 컨텍스트와 지연 로딩에 대해 심화 학습했습니다.")
                .studyDate(LocalDate.now())
                .category("DATABASE")
                .understanding("GOOD")
                .build();

        mockMvc.perform(post("/api/v1/logs")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchTarget)))
                .andExpect(status().isCreated());

        // When & Then
        mockMvc.perform(get("/api/v1/logs/search")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("title", "JPA")
                        .param("category", "DATABASE")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[*].title", everyItem(containsString("JPA"))))
                .andExpect(jsonPath("$.content[*].category", everyItem(is("DATABASE"))));
    }
}
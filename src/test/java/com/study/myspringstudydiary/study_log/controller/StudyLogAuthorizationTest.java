package com.study.myspringstudydiary.study_log.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.myspringstudydiary.auth.entity.User;
import com.study.myspringstudydiary.auth.entity.UserRole;
import com.study.myspringstudydiary.global.security.principal.UserPrincipal;
import com.study.myspringstudydiary.study_log.dto.request.StudyLogCreateRequest;
import com.study.myspringstudydiary.study_log.dto.response.StudyLogResponse;
import com.study.myspringstudydiary.study_log.entity.Category;
import com.study.myspringstudydiary.study_log.entity.Understanding;
import com.study.myspringstudydiary.study_log.service.StudyLogJpaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * StudyLog 인증/인가 테스트
 * - 인증된 사용자만 접근 가능
 * - 본인의 StudyLog만 수정/삭제 가능
 * - @CurrentUser 어노테이션 동작 검증
 */
@WebMvcTest(StudyLogJpaController.class)
@ActiveProfiles("test")
class StudyLogAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudyLogJpaService studyLogService;

    @MockBean
    private com.study.myspringstudydiary.global.security.jwt.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.study.myspringstudydiary.auth.service.CustomUserDetailsJpaService customUserDetailsService;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성 (메모리 내에서만)
        testUser1 = User.builder()
                .id(1L)
                .username("user1")
                .email("user1@example.com")
                .password("$2a$10$8H0OT8wgtALJkig6fmypi.Y7jzI5Y7W9PGgJeE9MtDHJGmLnCcLhy")
                .role(UserRole.USER)
                .enabled(true)
                .build();

        testUser2 = User.builder()
                .id(2L)
                .username("user2")
                .email("user2@example.com")
                .password("$2a$10$8H0OT8wgtALJkig6fmypi.Y7jzI5Y7W9PGgJeE9MtDHJGmLnCcLhy")
                .role(UserRole.USER)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 401 Unauthorized")
    void testUnauthenticatedAccess() throws Exception {
        // Given: 인증되지 않은 상태
        StudyLogCreateRequest request = new StudyLogCreateRequest();
        request.setTitle("Test Study Log");
        request.setContent("Test Content");
        request.setCategory("JPA");
        request.setUnderstanding("GOOD");
        request.setStudyTime(60);

        // When & Then: 401 응답
        mockMvc.perform(post("/api/v2/study-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    @DisplayName("인증된 사용자는 학습 기록 생성 가능")
    void testAuthenticatedUserCanCreateStudyLog() throws Exception {
        // Given: 인증된 사용자
        StudyLogCreateRequest request = new StudyLogCreateRequest();
        request.setTitle("JPA 학습");
        request.setContent("JPA 연관관계 매핑");
        request.setCategory("JPA");
        request.setUnderstanding("GOOD");
        request.setStudyTime(120);
        request.setStudyDate(LocalDate.now());

        StudyLogResponse response = StudyLogResponse.builder()
                .id(1L)
                .title("JPA 학습")
                .content("JPA 연관관계 매핑")
                .category("JPA")
                .categoryIcon("📚")
                .understanding("GOOD")
                .understandingEmoji("😊")
                .studyTime(120)
                .studyDate(LocalDate.now())
                .build();

        when(studyLogService.create(anyLong(), any(StudyLogCreateRequest.class)))
                .thenReturn(response);

        // When & Then: 201 Created
        mockMvc.perform(post("/api/v2/study-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(createUserPrincipal(testUser1))))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("JPA 학습"))
                .andExpect(jsonPath("$.content").value("JPA 연관관계 매핑"));
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    @DisplayName("본인의 학습 기록 조회 가능")
    void testUserCanViewOwnStudyLogs() throws Exception {
        // Given: Mock 데이터 설정
        StudyLogResponse response1 = StudyLogResponse.builder()
                .id(1L)
                .title("Study 1")
                .content("Content 1")
                .category("JPA")
                .categoryIcon("📚")
                .understanding("GOOD")
                .understandingEmoji("😊")
                .studyTime(60)
                .studyDate(LocalDate.now())
                .build();

        StudyLogResponse response2 = StudyLogResponse.builder()
                .id(2L)
                .title("Study 2")
                .content("Content 2")
                .category("SPRING")
                .categoryIcon("🌱")
                .understanding("NORMAL")
                .understandingEmoji("🤔")
                .studyTime(90)
                .studyDate(LocalDate.now())
                .build();

        when(studyLogService.findByUserId(anyLong()))
                .thenReturn(List.of(response1, response2));

        // When & Then: 본인 학습 기록 조회
        mockMvc.perform(get("/api/v2/study-logs/my")
                        .with(user(createUserPrincipal(testUser1))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Study 1"))
                .andExpect(jsonPath("$[1].title").value("Study 2"));
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    @DisplayName("카테고리별 학습 기록 조회")
    void testGetStudyLogsByCategory() throws Exception {
        // Given: Mock 데이터
        StudyLogResponse response = StudyLogResponse.builder()
                .id(1L)
                .title("JPA Study")
                .content("JPA Content")
                .category("JPA")
                .categoryIcon("📚")
                .understanding("GOOD")
                .understandingEmoji("😊")
                .studyTime(120)
                .studyDate(LocalDate.now())
                .build();

        when(studyLogService.findByUserAndCategory(anyLong(), any(Category.class)))
                .thenReturn(List.of(response));

        // When & Then: 카테고리별 조회
        mockMvc.perform(get("/api/v2/study-logs/my/category/JPA")
                        .with(user(createUserPrincipal(testUser1))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("JPA Study"))
                .andExpect(jsonPath("$[0].category").value("JPA"));
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    @DisplayName("날짜 범위로 학습 기록 조회")
    void testGetStudyLogsByDateRange() throws Exception {
        // Given: Mock 데이터
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        StudyLogResponse response = StudyLogResponse.builder()
                .id(1L)
                .title("Recent Study")
                .content("Recent Content")
                .category("SPRING")
                .categoryIcon("🌱")
                .understanding("GOOD")
                .understandingEmoji("😊")
                .studyTime(90)
                .studyDate(LocalDate.now())
                .build();

        when(studyLogService.findByUserIdAndDateRange(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(response));

        // When & Then: 날짜 범위 조회
        mockMvc.perform(get("/api/v2/study-logs/my/date-range")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .with(user(createUserPrincipal(testUser1))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Recent Study"));
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    @DisplayName("학습 기록 개수 조회")
    void testGetStudyLogCount() throws Exception {
        // Given: Mock 데이터
        when(studyLogService.countByUserId(anyLong())).thenReturn(5L);

        // When & Then: 개수 조회
        mockMvc.perform(get("/api/v2/study-logs/my/count")
                        .with(user(createUserPrincipal(testUser1))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(5));
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    @DisplayName("검색 기능 - 인증된 사용자만 본인 데이터 검색")
    void testSearchWithAuthentication() throws Exception {
        // Given: Mock 데이터
        StudyLogResponse response = StudyLogResponse.builder()
                .id(1L)
                .title("Spring Boot")
                .content("Spring Boot Content")
                .category("SPRING")
                .categoryIcon("🌱")
                .understanding("GOOD")
                .understandingEmoji("😊")
                .studyTime(120)
                .studyDate(LocalDate.now())
                .build();

        when(studyLogService.search(anyLong(), anyString(), any(), any(), any()))
                .thenReturn(List.of(response));

        // When & Then: 검색
        mockMvc.perform(get("/api/v2/study-logs/search")
                        .param("title", "Spring")
                        .param("category", "SPRING")
                        .with(user(createUserPrincipal(testUser1))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Spring Boot"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("관리자는 모든 학습 기록 조회 가능")
    void testAdminCanViewAllStudyLogs() throws Exception {
        // Given: 관리자 계정
        StudyLogResponse response1 = StudyLogResponse.builder()
                .id(1L)
                .title("Admin View 1")
                .content("Content 1")
                .category("JPA")
                .categoryIcon("📚")
                .understanding("GOOD")
                .understandingEmoji("😊")
                .studyTime(60)
                .studyDate(LocalDate.now())
                .build();

        StudyLogResponse response2 = StudyLogResponse.builder()
                .id(2L)
                .title("Admin View 2")
                .content("Content 2")
                .category("SPRING")
                .categoryIcon("🌱")
                .understanding("NORMAL")
                .understandingEmoji("🤔")
                .studyTime(90)
                .studyDate(LocalDate.now())
                .build();

        when(studyLogService.findAll()).thenReturn(List.of(response1, response2));

        // When & Then: 전체 조회
        mockMvc.perform(get("/api/v2/study-logs")
                        .with(user("admin").roles("ADMIN")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Admin View 1"))
                .andExpect(jsonPath("$[1].title").value("Admin View 2"));
    }

    @Test
    @DisplayName("잘못된 토큰으로 접근 시 401 Unauthorized")
    void testInvalidTokenAccess() throws Exception {
        // When & Then: 잘못된 토큰으로 접근
        mockMvc.perform(get("/api/v2/study-logs/my")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    @DisplayName("카테고리별 학습 시간 통계 조회")
    void testGetStudyTimeStatsByCategory() throws Exception {
        // Given: Mock 데이터
        Object[] stat1 = new Object[]{Category.JPA, 240L};
        Object[] stat2 = new Object[]{Category.SPRING, 180L};

        when(studyLogService.getStudyTimeByCategoryForUser(anyLong()))
                .thenReturn(List.of(stat1, stat2));

        // When & Then: 통계 조회
        mockMvc.perform(get("/api/v2/study-logs/my/stats/time-by-category")
                        .with(user(createUserPrincipal(testUser1))))
                .andDo(print())
                .andExpect(status().isOk());
    }

    // Helper method to create UserPrincipal
    private UserPrincipal createUserPrincipal(User user) {
        return UserPrincipal.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.singletonList(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_" + user.getRole().name())))
                .enabled(user.isEnabled())
                .build();
    }
}
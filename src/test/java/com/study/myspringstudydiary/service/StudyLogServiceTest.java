package com.study.myspringstudydiary.service;

import com.study.myspringstudydiary.dto.request.StudyLogCreateRequest;
import com.study.myspringstudydiary.dto.request.StudyLogUpdateRequest;
import com.study.myspringstudydiary.dto.response.StudyLogResponse;
import com.study.myspringstudydiary.dto.response.StudyLogDeleteResponse;
import com.study.myspringstudydiary.exception.ForbiddenException;
import com.study.myspringstudydiary.exception.ResourceNotFoundException;
import com.study.myspringstudydiary.entity.Category;
import com.study.myspringstudydiary.entity.StudyLog;
import com.study.myspringstudydiary.entity.Understanding;
import com.study.myspringstudydiary.dao.StudyLogDao;
import com.study.myspringstudydiary.security.CustomUserDetails;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudyLog Service 단위 테스트")
class StudyLogServiceTest {

    // Mockito의 Strict stubbing 모드에서는 사용되지 않는 stubbing이 있으면 에러 발생
    // lenient()를 사용하여 특정 stubbing을 lenient하게 설정할 수 있음

    @Mock
    private StudyLogDao studyLogDao;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private CustomUserDetails customUserDetails;

    @InjectMocks
    private StudyLogService studyLogService;

    private StudyLog testStudyLog;
    private final Long TEST_USER_ID = 1L;
    private final Long TEST_STUDY_LOG_ID = 100L;

    @BeforeEach
    void setUp() {
        // SecurityContext 설정 (lenient로 설정하여 모든 테스트에서 사용되지 않아도 에러 발생 안함)
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        lenient().when(authentication.getPrincipal()).thenReturn(customUserDetails);
        lenient().when(customUserDetails.getUserId()).thenReturn(TEST_USER_ID);

        // 테스트 데이터 준비
        testStudyLog = new StudyLog();
        testStudyLog.setId(TEST_STUDY_LOG_ID);
        testStudyLog.setUserId(TEST_USER_ID);
        testStudyLog.setTitle("Spring Boot 학습");
        testStudyLog.setContent("JUnit과 Mockito 학습 완료");
        testStudyLog.setCategory(Category.SPRING);
        testStudyLog.setUnderstanding(Understanding.VERY_GOOD);
        testStudyLog.setStudyTime(120);
        testStudyLog.setStudyDate(LocalDate.now());
        testStudyLog.setCreatedAt(LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("학습 기록 생성")
    class CreateStudyLog {

        @Test
        @DisplayName("성공: 유효한 데이터로 학습 기록 생성")
        void createStudyLog_Success() {
            // Given
            StudyLogCreateRequest request = new StudyLogCreateRequest();
            request.setTitle("Spring Boot 학습");
            request.setContent("JUnit과 Mockito 학습 완료");
            request.setCategory("SPRING");
            request.setUnderstanding("VERY_GOOD");
            request.setStudyTime(120);
            request.setStudyDate(LocalDate.now());

            when(studyLogDao.save(any(StudyLog.class))).thenReturn(testStudyLog);

            // When
            StudyLogResponse response = studyLogService.createStudyLog(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("Spring Boot 학습");
            assertThat(response.getCategory()).isEqualTo("SPRING");
            assertThat(response.getStudyTime()).isEqualTo(120);

            verify(studyLogDao, times(1)).save(any(StudyLog.class));
        }

        @Test
        @DisplayName("실패: 인증되지 않은 사용자")
        void createStudyLog_NotAuthenticated() {
            // Given
            when(authentication.isAuthenticated()).thenReturn(false);
            StudyLogCreateRequest request = new StudyLogCreateRequest();
            request.setTitle("Test");
            request.setContent("Test Content");
            request.setCategory("SPRING");
            request.setUnderstanding("VERY_GOOD");
            request.setStudyTime(60);
            request.setStudyDate(LocalDate.now());

            // When & Then
            assertThatThrownBy(() -> studyLogService.createStudyLog(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User is not authenticated");

            // Verify that save was never called due to authentication failure
            verify(studyLogDao, never()).save(any(StudyLog.class));
        }
    }

    @Nested
    @DisplayName("학습 기록 조회")
    class GetStudyLog {

        @Test
        @DisplayName("성공: ID로 학습 기록 조회")
        void getStudyLogById_Success() {
            // Given
            when(studyLogDao.findById(TEST_STUDY_LOG_ID))
                .thenReturn(Optional.of(testStudyLog));

            // When
            StudyLogResponse response = studyLogService.getStudyLogById(TEST_STUDY_LOG_ID);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("Spring Boot 학습");
            verify(studyLogDao, times(1)).findById(TEST_STUDY_LOG_ID);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 학습 기록")
        void getStudyLogById_NotFound() {
            // Given
            when(studyLogDao.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> studyLogService.getStudyLogById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Study Log");
        }

        @Test
        @DisplayName("실패: 다른 사용자의 학습 기록 접근 시도")
        void getStudyLogById_Forbidden() {
            // Given
            testStudyLog.setUserId(2L); // 다른 사용자 ID
            when(studyLogDao.findById(TEST_STUDY_LOG_ID))
                .thenReturn(Optional.of(testStudyLog));

            // When & Then
            assertThatThrownBy(() -> studyLogService.getStudyLogById(TEST_STUDY_LOG_ID))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Study Log");
        }

        @Test
        @DisplayName("성공: 모든 학습 기록 조회")
        void getAllStudyLogs_Success() {
            // Given
            List<StudyLog> studyLogs = Arrays.asList(testStudyLog);
            when(studyLogDao.findByUserId(TEST_USER_ID)).thenReturn(studyLogs);

            // When
            List<StudyLogResponse> responses = studyLogService.getAllStudyLogs();

            // Then
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getTitle()).isEqualTo("Spring Boot 학습");
            verify(studyLogDao, times(1)).findByUserId(TEST_USER_ID);
        }
    }

    @Nested
    @DisplayName("학습 기록 수정")
    class UpdateStudyLog {

        @Test
        @DisplayName("성공: 부분 업데이트")
        void updateStudyLog_PartialUpdate() {
            // Given
            StudyLogUpdateRequest request = new StudyLogUpdateRequest();
            request.setTitle("수정된 제목");
            request.setStudyTime(180);

            when(studyLogDao.findById(TEST_STUDY_LOG_ID))
                .thenReturn(Optional.of(testStudyLog));
            when(studyLogDao.update(any(StudyLog.class))).thenReturn(testStudyLog);

            // When
            StudyLogResponse response = studyLogService.updateStudyLog(
                TEST_STUDY_LOG_ID, request);

            // Then
            assertThat(response).isNotNull();
            verify(studyLogDao, times(1)).update(any(StudyLog.class));
        }

        @Test
        @DisplayName("실패: 권한 없음")
        void updateStudyLog_Forbidden() {
            // Given
            testStudyLog.setUserId(2L); // 다른 사용자
            StudyLogUpdateRequest request = new StudyLogUpdateRequest();
            request.setTitle("수정 시도");

            when(studyLogDao.findById(TEST_STUDY_LOG_ID))
                .thenReturn(Optional.of(testStudyLog));

            // When & Then
            assertThatThrownBy(() ->
                studyLogService.updateStudyLog(TEST_STUDY_LOG_ID, request))
                .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("실패: 수정할 내용이 없음")
        void updateStudyLog_NoUpdates() {
            // Given
            StudyLogUpdateRequest request = mock(StudyLogUpdateRequest.class);
            when(request.hasNoUpdates()).thenReturn(true);

            when(studyLogDao.findById(TEST_STUDY_LOG_ID))
                .thenReturn(Optional.of(testStudyLog));

            // When & Then
            assertThatThrownBy(() ->
                studyLogService.updateStudyLog(TEST_STUDY_LOG_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수정할 내용이 없습니다");
        }
    }

    @Nested
    @DisplayName("학습 기록 삭제")
    class DeleteStudyLog {

        @Test
        @DisplayName("성공: 학습 기록 삭제")
        void deleteStudyLog_Success() {
            // Given
            when(studyLogDao.findById(TEST_STUDY_LOG_ID))
                .thenReturn(Optional.of(testStudyLog));
            when(studyLogDao.deleteById(TEST_STUDY_LOG_ID)).thenReturn(true);

            // When
            StudyLogDeleteResponse response = studyLogService.deleteStudyLog(TEST_STUDY_LOG_ID);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getDeletedId()).isEqualTo(TEST_STUDY_LOG_ID);
            verify(studyLogDao, times(1)).deleteById(TEST_STUDY_LOG_ID);
        }

        @Test
        @DisplayName("실패: 다른 사용자의 학습 기록 삭제 시도")
        void deleteStudyLog_Forbidden() {
            // Given
            testStudyLog.setUserId(2L); // 다른 사용자
            when(studyLogDao.findById(TEST_STUDY_LOG_ID))
                .thenReturn(Optional.of(testStudyLog));

            // When & Then
            assertThatThrownBy(() ->
                studyLogService.deleteStudyLog(TEST_STUDY_LOG_ID))
                .isInstanceOf(ForbiddenException.class);

            verify(studyLogDao, never()).deleteById(any());
        }
    }

    @Test
    @DisplayName("학습 기록 개수 조회")
    void getStudyLogCount() {
        // Given
        // Note: count() method doesn't require authentication in current implementation
        when(studyLogDao.count()).thenReturn(10L);

        // When
        long count = studyLogService.getStudyLogCount();

        // Then
        assertThat(count).isEqualTo(10L);
        verify(studyLogDao, times(1)).count();
    }
}
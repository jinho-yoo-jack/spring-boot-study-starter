package com.study.myspringstudydiary.study_log.service;

import com.study.myspringstudydiary.auth.entity.User;
import com.study.myspringstudydiary.auth.entity.UserRole;
import com.study.myspringstudydiary.auth.repository.UserRepository;
import com.study.myspringstudydiary.study_log.dto.request.StudyLogUpdateRequest;
import com.study.myspringstudydiary.study_log.entity.Category;
import com.study.myspringstudydiary.study_log.entity.StudyLog;
import com.study.myspringstudydiary.study_log.entity.Understanding;
import com.study.myspringstudydiary.study_log.exception.AccessDeniedException;
import com.study.myspringstudydiary.study_log.exception.ResourceNotFoundException;
import com.study.myspringstudydiary.study_log.repository.StudyLogRepository;
import com.study.myspringstudydiary.global.mapper.StudyLogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * StudyLog 서비스 소유자 검증 테스트
 * - 본인의 StudyLog만 수정/삭제 가능
 * - 다른 사용자의 StudyLog 접근 시 예외 발생
 */
@ExtendWith(MockitoExtension.class)
class StudyLogOwnerValidationTest {

    @Mock
    private StudyLogRepository studyLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudyLogMapper studyLogMapper;

    @InjectMocks
    private StudyLogJpaService studyLogService;

    private User owner;
    private User otherUser;
    private StudyLog studyLog;

    @BeforeEach
    void setUp() {
        // 소유자 사용자
        owner = User.builder()
                .id(1L)
                .username("owner")
                .email("owner@example.com")
                .password("password")
                .role(UserRole.USER)
                .enabled(true)
                .build();

        // 다른 사용자
        otherUser = User.builder()
                .id(2L)
                .username("other")
                .email("other@example.com")
                .password("password")
                .role(UserRole.USER)
                .enabled(true)
                .build();

        // StudyLog (owner가 소유)
        studyLog = StudyLog.builder()
                .id(100L)
                .user(owner)
                .title("Original Title")
                .content("Original Content")
                .category(Category.JPA)
                .understanding(Understanding.GOOD)
                .studyTime(120)
                .studyDate(LocalDate.now())
                .build();
    }

    @Test
    @DisplayName("소유자는 자신의 StudyLog 수정 가능")
    void testOwnerCanUpdateOwnStudyLog() {
        // Given
        Long studyLogId = 100L;
        Long ownerId = owner.getId();

        StudyLogUpdateRequest updateRequest = new StudyLogUpdateRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setContent("Updated Content");

        when(studyLogRepository.findById(studyLogId)).thenReturn(Optional.of(studyLog));
        when(studyLogMapper.toResponse(any(StudyLog.class))).thenReturn(null);

        // When
        studyLogService.update(studyLogId, ownerId, updateRequest);

        // Then
        verify(studyLogRepository).findById(studyLogId);
        assertThat(studyLog.getTitle()).isEqualTo("Updated Title");
        assertThat(studyLog.getContent()).isEqualTo("Updated Content");
    }

    @Test
    @DisplayName("다른 사용자는 StudyLog 수정 불가 - AccessDeniedException 발생")
    void testOtherUserCannotUpdateStudyLog() {
        // Given
        Long studyLogId = 100L;
        Long otherUserId = otherUser.getId();

        StudyLogUpdateRequest updateRequest = new StudyLogUpdateRequest();
        updateRequest.setTitle("Hacked Title");

        when(studyLogRepository.findById(studyLogId)).thenReturn(Optional.of(studyLog));

        // When & Then
        assertThatThrownBy(() ->
                studyLogService.update(studyLogId, otherUserId, updateRequest)
        )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("이 학습 일지를 수정할 권한이 없습니다.");

        // 실제 수정이 일어나지 않음
        assertThat(studyLog.getTitle()).isEqualTo("Original Title");
    }

    @Test
    @DisplayName("소유자는 자신의 StudyLog 삭제 가능")
    void testOwnerCanDeleteOwnStudyLog() {
        // Given
        Long studyLogId = 100L;
        Long ownerId = owner.getId();

        when(studyLogRepository.findById(studyLogId)).thenReturn(Optional.of(studyLog));

        // When
        studyLogService.delete(studyLogId, ownerId);

        // Then
        verify(studyLogRepository).findById(studyLogId);
        verify(studyLogRepository).delete(studyLog);
    }

    @Test
    @DisplayName("다른 사용자는 StudyLog 삭제 불가 - AccessDeniedException 발생")
    void testOtherUserCannotDeleteStudyLog() {
        // Given
        Long studyLogId = 100L;
        Long otherUserId = otherUser.getId();

        when(studyLogRepository.findById(studyLogId)).thenReturn(Optional.of(studyLog));

        // When & Then
        assertThatThrownBy(() ->
                studyLogService.delete(studyLogId, otherUserId)
        )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("이 학습 일지를 삭제할 권한이 없습니다.");

        // 실제 삭제가 일어나지 않음
        verify(studyLogRepository, never()).delete(any(StudyLog.class));
    }

    @Test
    @DisplayName("소유자는 자신의 StudyLog 조회 가능 (소유자 검증 포함)")
    void testOwnerCanViewOwnStudyLogWithOwnerCheck() {
        // Given
        Long studyLogId = 100L;
        Long ownerId = owner.getId();

        when(studyLogRepository.findByIdWithUser(studyLogId)).thenReturn(Optional.of(studyLog));
        when(studyLogMapper.toResponse(any(StudyLog.class))).thenReturn(null);

        // When
        studyLogService.findByIdWithOwnerCheck(studyLogId, ownerId);

        // Then
        verify(studyLogRepository).findByIdWithUser(studyLogId);
        verify(studyLogMapper).toResponse(studyLog);
    }

    @Test
    @DisplayName("다른 사용자는 StudyLog 조회 불가 (소유자 검증 포함) - AccessDeniedException 발생")
    void testOtherUserCannotViewStudyLogWithOwnerCheck() {
        // Given
        Long studyLogId = 100L;
        Long otherUserId = otherUser.getId();

        when(studyLogRepository.findByIdWithUser(studyLogId)).thenReturn(Optional.of(studyLog));

        // When & Then
        assertThatThrownBy(() ->
                studyLogService.findByIdWithOwnerCheck(studyLogId, otherUserId)
        )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("이 학습 일지를 조회할 권한이 없습니다.");

        verify(studyLogMapper, never()).toResponse(any(StudyLog.class));
    }

    @Test
    @DisplayName("존재하지 않는 StudyLog 수정 시도 - ResourceNotFoundException 발생")
    void testUpdateNonExistentStudyLog() {
        // Given
        Long nonExistentId = 999L;
        Long userId = owner.getId();

        StudyLogUpdateRequest updateRequest = new StudyLogUpdateRequest();
        updateRequest.setTitle("New Title");

        when(studyLogRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() ->
                studyLogService.update(nonExistentId, userId, updateRequest)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("StudyLog not found with id: " + nonExistentId);
    }

    @Test
    @DisplayName("존재하지 않는 StudyLog 삭제 시도 - ResourceNotFoundException 발생")
    void testDeleteNonExistentStudyLog() {
        // Given
        Long nonExistentId = 999L;
        Long userId = owner.getId();

        when(studyLogRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() ->
                studyLogService.delete(nonExistentId, userId)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("StudyLog not found with id: " + nonExistentId);
    }

    @Test
    @DisplayName("null userId로 수정 시도 시 AccessDeniedException 발생")
    void testUpdateWithNullUserId() {
        // Given
        Long studyLogId = 100L;
        Long nullUserId = null;

        StudyLogUpdateRequest updateRequest = new StudyLogUpdateRequest();
        updateRequest.setTitle("New Title");

        when(studyLogRepository.findById(studyLogId)).thenReturn(Optional.of(studyLog));

        // When & Then - null userId도 접근 거부로 처리됨
        assertThatThrownBy(() ->
                studyLogService.update(studyLogId, nullUserId, updateRequest)
        )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("이 학습 일지를 수정할 권한이 없습니다.");
    }

    @Test
    @DisplayName("User ID가 일치하지 않으면 AccessDeniedException 발생 - 보안 테스트")
    void testSecurityCheckForDifferentUserId() {
        // Given: 악의적인 사용자가 다른 사용자 ID로 접근
        Long studyLogId = 100L;
        Long hackerUserId = 9999L; // 존재하지 않는 사용자 ID

        StudyLogUpdateRequest updateRequest = new StudyLogUpdateRequest();
        updateRequest.setTitle("Hacked!");

        when(studyLogRepository.findById(studyLogId)).thenReturn(Optional.of(studyLog));

        // When & Then
        assertThatThrownBy(() ->
                studyLogService.update(studyLogId, hackerUserId, updateRequest)
        )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("이 학습 일지를 수정할 권한이 없습니다.");

        // 원본 데이터가 변경되지 않음
        assertThat(studyLog.getTitle()).isEqualTo("Original Title");
    }
}
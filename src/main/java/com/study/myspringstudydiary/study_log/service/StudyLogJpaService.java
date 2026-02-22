package com.study.myspringstudydiary.study_log.service;

import com.study.myspringstudydiary.auth.entity.User;
import com.study.myspringstudydiary.auth.repository.UserRepository;
import com.study.myspringstudydiary.study_log.dto.request.StudyLogCreateRequest;
import com.study.myspringstudydiary.study_log.dto.request.StudyLogUpdateRequest;
import com.study.myspringstudydiary.study_log.dto.response.StudyLogResponse;
import com.study.myspringstudydiary.study_log.dto.response.StudyLogDeleteResponse;
import com.study.myspringstudydiary.study_log.entity.Category;
import com.study.myspringstudydiary.study_log.entity.StudyLog;
import com.study.myspringstudydiary.study_log.entity.Understanding;
import com.study.myspringstudydiary.study_log.exception.AccessDeniedException;
import com.study.myspringstudydiary.study_log.exception.ResourceNotFoundException;
import com.study.myspringstudydiary.study_log.repository.StudyLogRepository;
import com.study.myspringstudydiary.global.mapper.StudyLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Study Log Service with Spring Data JPA
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StudyLogJpaService {

    private final StudyLogRepository studyLogRepository;
    private final UserRepository userRepository;
    private final StudyLogMapper studyLogMapper;

    // ========== CREATE ==========

    @Transactional
    public StudyLogResponse create(Long userId, StudyLogCreateRequest request) {
        log.info("Creating study log: {} for user: {}", request.getTitle(), userId);

        // 1. User 엔티티 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // 2. StudyLog 생성 및 User 설정
        StudyLog studyLog = StudyLog.builder()
            .user(user)  // User 참조 설정
            .title(request.getTitle())
            .content(request.getContent())
            .category(Category.valueOf(request.getCategory()))
            .understanding(Understanding.valueOf(request.getUnderstanding()))
            .studyTime(request.getStudyTime())
            .studyDate(request.getStudyDate() != null ? request.getStudyDate() : LocalDate.now())
            .build();

        // 3. 저장
        StudyLog saved = studyLogRepository.save(studyLog);
        log.info("Study log created with ID: {} for user: {}", saved.getId(), user.getUsername());

        return studyLogMapper.toResponse(saved);
    }

    // ========== READ ==========

    /**
     * 모든 학습 일지 조회
     */
    public List<StudyLogResponse> findAll() {
        return studyLogRepository.findAll().stream()
            .map(studyLogMapper::toResponse)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 카테고리별 조회
     */
    public List<StudyLogResponse> findByCategory(Category category) {
        return studyLogRepository.findByCategory(category).stream()
            .map(studyLogMapper::toResponse)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 이해도별 조회
     */
    public List<StudyLogResponse> findByUnderstanding(Understanding understanding) {
        return studyLogRepository.findByUnderstanding(understanding).stream()
            .map(studyLogMapper::toResponse)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 날짜 범위 검색
     */
    public List<StudyLogResponse> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return studyLogRepository.findByDateRange(startDate, endDate).stream()
            .map(studyLogMapper::toResponse)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 검색 조건 (User 필터링 추가)
     */
    public List<StudyLogResponse> search(Long userId, String title, Category category,
                                        LocalDate startDate, LocalDate endDate) {
        return studyLogRepository.searchWithConditions(userId, title, category, startDate, endDate).stream()
            .map(studyLogMapper::toResponse)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * ID로 단건 조회
     */
    public StudyLogResponse findById(Long id) {
        StudyLog studyLog = studyLogRepository.findByIdOptimized(id)
            .orElseThrow(() -> new ResourceNotFoundException("StudyLog not found with id: " + id));
        return studyLogMapper.toResponse(studyLog);
    }

    /**
     * ID로 단건 조회 (소유자 검증 포함)
     */
    public StudyLogResponse findByIdWithOwnerCheck(Long id, Long userId) {
        StudyLog studyLog = studyLogRepository.findByIdWithUser(id)
            .orElseThrow(() -> new ResourceNotFoundException("StudyLog not found with id: " + id));

        // 소유자 검증
        if (!studyLog.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("이 학습 일지를 조회할 권한이 없습니다.");
        }

        return studyLogMapper.toResponse(studyLog);
    }

    /**
     * 오늘의 학습 일지 조회
     */
    public List<StudyLogResponse> findTodayStudyLogs() {
        return studyLogRepository.findTodayStudyLogs().stream()
            .map(studyLogMapper::toResponse)
            .collect(java.util.stream.Collectors.toList());
    }

    // ========== UPDATE ==========

    @Transactional
    public StudyLogResponse update(Long id, Long userId, StudyLogUpdateRequest request) {
        log.info("Updating study log with id: {} by user: {}", id, userId);

        StudyLog studyLog = studyLogRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("StudyLog not found with id: " + id));

        // 소유자 검증
        if (!studyLog.getUser().getId().equals(userId)) {
            log.warn("User {} attempted to update study log {} owned by user {}",
                userId, id, studyLog.getUser().getId());
            throw new AccessDeniedException("이 학습 일지를 수정할 권한이 없습니다.");
        }

        // Dirty Checking을 통한 업데이트
        if (request.getTitle() != null) {
            studyLog.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            studyLog.setContent(request.getContent());
        }
        if (request.getCategory() != null) {
            studyLog.setCategory(Category.valueOf(request.getCategory()));
        }
        if (request.getUnderstanding() != null) {
            studyLog.setUnderstanding(Understanding.valueOf(request.getUnderstanding()));
        }
        if (request.getStudyTime() != null) {
            studyLog.setStudyTime(request.getStudyTime());
        }
        if (request.getStudyDate() != null) {
            studyLog.setStudyDate(request.getStudyDate());
        }

        // 트랜잭션 커밋 시점에 자동으로 UPDATE 쿼리 실행
        return studyLogMapper.toResponse(studyLog);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void bulkUpdateHours(List<Long> ids, Integer hours) {
        for (Long id : ids) {
            studyLogRepository.updateStudyHours(id, hours);
        }
    }

    // ========== DELETE ==========

    @Transactional
    public StudyLogDeleteResponse delete(Long id, Long userId) {
        log.info("Deleting study log with id: {} by user: {}", id, userId);

        StudyLog studyLog = studyLogRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("StudyLog not found with id: " + id));

        // 소유자 검증
        if (!studyLog.getUser().getId().equals(userId)) {
            log.warn("User {} attempted to delete study log {} owned by user {}",
                userId, id, studyLog.getUser().getId());
            throw new AccessDeniedException("이 학습 일지를 삭제할 권한이 없습니다.");
        }

        studyLogRepository.delete(studyLog);

        return StudyLogDeleteResponse.builder()
            .deletedId(id)
            .message("Study log deleted successfully")
            .build();
    }

    // ========== STATISTICS ==========

    /**
     * 카테고리별 학습 일지 개수 (캐싱 적용)
     */
    @Cacheable(value = "categoryCount", key = "#category")
    public long countByCategory(Category category) {
        return studyLogRepository.countByCategory(category);
    }

    /**
     * 카테고리별 학습 시간 통계
     */
    public List<Object[]> getStudyTimeByCategory() {
        return studyLogRepository.findStudyTimeByCategory();
    }

    /**
     * 전문 검색 (MySQL Full-text search)
     */
    public List<StudyLogResponse> searchFullText(String keyword) {
        return studyLogRepository.searchFullText(keyword).stream()
            .map(studyLogMapper::toResponse)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 캐시 무효화
     */
    @CacheEvict(value = "categoryCount", allEntries = true)
    @Transactional
    public void evictCache() {
        log.info("Cache evicted for categoryCount");
    }

    // ========== USER 관련 메서드 ==========

    /**
     * 특정 사용자의 학습 일지 조회
     */
    public List<StudyLogResponse> findByUserId(Long userId) {
        return studyLogRepository.findByUserId(userId).stream()
            .map(studyLogMapper::toResponse)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 특정 사용자의 학습 일지 조회 (Fetch Join으로 N+1 방지)
     */
    public List<StudyLogResponse> findByUserIdWithUser(Long userId) {
        return studyLogRepository.findByUserIdWithUser(userId).stream()
            .map(studyLogMapper::toResponse)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 사용자별 카테고리 학습 일지 조회
     */
    public List<StudyLogResponse> findByUserAndCategory(Long userId, Category category) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        return studyLogRepository.findByUserAndCategory(user, category).stream()
            .map(studyLogMapper::toResponse)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 사용자별 날짜 범위 학습 일지 조회
     */
    public List<StudyLogResponse> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return studyLogRepository.findByUserIdAndStudyDateBetween(userId, startDate, endDate).stream()
            .map(studyLogMapper::toResponse)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 사용자별 학습 일지 개수
     */
    public long countByUserId(Long userId) {
        return studyLogRepository.countByUserId(userId);
    }

    /**
     * 사용자별 카테고리 학습 시간 통계
     */
    public List<Object[]> getStudyTimeByCategoryForUser(Long userId) {
        return studyLogRepository.findStudyTimeByCategoryForUser(userId);
    }

    /**
     * 학습 일지 작성자 정보 조회
     */
    public User getStudyLogAuthor(Long studyLogId) {
        StudyLog studyLog = studyLogRepository.findByIdWithUser(studyLogId)
            .orElseThrow(() -> new ResourceNotFoundException("StudyLog", studyLogId));
        return studyLog.getUser();
    }

    /**
     * 양방향 관계 활용 - User 엔티티를 통한 StudyLog 생성
     */
    @Transactional
    public StudyLogResponse createWithBidirectional(Long userId, StudyLogCreateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        StudyLog studyLog = StudyLog.builder()
            .title(request.getTitle())
            .content(request.getContent())
            .category(Category.valueOf(request.getCategory()))
            .understanding(Understanding.valueOf(request.getUnderstanding()))
            .studyTime(request.getStudyTime())
            .studyDate(request.getStudyDate() != null ? request.getStudyDate() : LocalDate.now())
            .build();

        // 양방향 관계 설정 (편의 메서드 사용)
        user.addStudyLog(studyLog);

        // CascadeType.ALL이면 자동 저장, 아니면 명시적 저장
        StudyLog saved = studyLogRepository.save(studyLog);

        return studyLogMapper.toResponse(saved);
    }
}
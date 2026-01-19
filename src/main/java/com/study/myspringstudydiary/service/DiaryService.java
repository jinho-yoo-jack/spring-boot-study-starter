package com.study.myspringstudydiary.service;

import com.study.myspringstudydiary.dto.DiaryRequest;
import com.study.myspringstudydiary.dto.DiaryResponse;
import com.study.myspringstudydiary.entity.Diary;
import com.study.myspringstudydiary.entity.User;
import com.study.myspringstudydiary.exception.ResourceNotFoundException;
import com.study.myspringstudydiary.repository.DiaryRepository;
import com.study.myspringstudydiary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;

    @Transactional
    public DiaryResponse createDiary(DiaryRequest request) {
        log.debug("Creating new diary for user id: {}", request.getUserId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Diary diary = Diary.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .user(user)
                .build();

        Diary savedDiary = diaryRepository.save(diary);
        log.info("Diary created successfully with id: {}", savedDiary.getId());

        return DiaryResponse.from(savedDiary);
    }

    public DiaryResponse getDiaryById(Long id) {
        log.debug("Fetching diary with id: {}", id);
        Diary diary = diaryRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diary not found with id: " + id));
        return DiaryResponse.from(diary);
    }

    public Page<DiaryResponse> getDiariesByUserId(Long userId, Pageable pageable) {
        log.debug("Fetching diaries for user id: {}", userId);
        return diaryRepository.findByUserId(userId, pageable)
                .map(DiaryResponse::from);
    }

    public List<DiaryResponse> getDiariesByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching diaries for user id: {} between {} and {}", userId, startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        return diaryRepository.findByUserIdAndDateRange(userId, startDateTime, endDateTime)
                .stream()
                .map(DiaryResponse::from)
                .collect(Collectors.toList());
    }

    public Page<DiaryResponse> searchDiaries(String keyword, Pageable pageable) {
        log.debug("Searching diaries with keyword: {}", keyword);
        return diaryRepository.searchByKeyword(keyword, pageable)
                .map(DiaryResponse::from);
    }

    @Transactional
    public DiaryResponse updateDiary(Long id, DiaryRequest request) {
        log.debug("Updating diary with id: {}", id);

        Diary diary = diaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diary not found with id: " + id));

        diary.updateDiary(request.getTitle(), request.getContent());

        Diary updatedDiary = diaryRepository.save(diary);
        log.info("Diary updated successfully with id: {}", updatedDiary.getId());

        return DiaryResponse.from(updatedDiary);
    }

    @Transactional
    public void deleteDiary(Long id) {
        log.debug("Deleting diary with id: {}", id);

        if (!diaryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Diary not found with id: " + id);
        }

        diaryRepository.deleteById(id);
        log.info("Diary deleted successfully with id: {}", id);
    }

    public Long getDiaryCountByUserId(Long userId) {
        log.debug("Counting diaries for user id: {}", userId);
        return diaryRepository.countByUserId(userId);
    }
}
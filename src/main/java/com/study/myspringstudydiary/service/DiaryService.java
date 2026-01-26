package com.study.myspringstudydiary.service;

import com.study.myspringstudydiary.dto.DiaryRequest;
import com.study.myspringstudydiary.dto.DiaryResponse;
import com.study.myspringstudydiary.exception.ResourceNotFoundException;
import com.study.myspringstudydiary.model.Diary;
import com.study.myspringstudydiary.model.User;
import com.study.myspringstudydiary.repository.DiaryMapRepository;
import com.study.myspringstudydiary.repository.UserMapRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryMapRepository diaryRepository;
    private final UserMapRepository userRepository;

    public DiaryResponse createDiary(DiaryRequest request) {
        log.debug("Creating new diary for user id: {}", request.getUserId());

        User user = userRepository.findById(request.getUserId());
        if (user == null) {
            throw new ResourceNotFoundException("User not found with id: " + request.getUserId());
        }

        Diary diary = Diary.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .userId(user.getId())
                .userName(user.getUserName())
                .build();

        Diary savedDiary = diaryRepository.save(diary);
        log.info("Diary created successfully with id: {}", savedDiary.getId());

        return DiaryResponse.from(savedDiary);
    }
}
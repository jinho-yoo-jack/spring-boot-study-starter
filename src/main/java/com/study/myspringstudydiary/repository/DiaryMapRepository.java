package com.study.myspringstudydiary.repository;

import com.study.myspringstudydiary.model.Diary;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class DiaryMapRepository {

    private final Map<Long, Diary> diaries = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Diary save(Diary diary) {
        if (diary.getId() == null) {
            // 새 다이어리 생성
            diary.setId(idGenerator.getAndIncrement());
            diary.setCreatedAt(LocalDateTime.now());
        }
        diary.setUpdatedAt(LocalDateTime.now());
        diaries.put(diary.getId(), diary);
        return diary;
    }

    public Diary findById(Long id) {
        return diaries.get(id);
    }
}
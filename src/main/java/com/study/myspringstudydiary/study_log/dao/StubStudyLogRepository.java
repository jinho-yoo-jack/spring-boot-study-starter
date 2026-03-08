package com.study.myspringstudydiary.study_log.dao;

import com.study.myspringstudydiary.study_log.entity.StudyLog;

public class StubStudyLogRepository {
    public StudyLog findById(Long id) {
        return new StudyLog();
    }
}

package com.study.myspringstudydiary.exception;

/**
 * 학습 일지를 찾을 수 없을 때 발생하는 예외
 */
public class StudyLogNotFoundException extends RuntimeException {

    public StudyLogNotFoundException(Long id) {
        super("해당 학습 일지를 찾을 수 없습니다. (id: " + id + ")");
    }
}
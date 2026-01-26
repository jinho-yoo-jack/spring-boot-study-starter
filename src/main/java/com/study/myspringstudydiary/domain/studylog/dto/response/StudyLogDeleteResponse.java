package com.study.myspringstudydiary.domain.studylog.dto.response;

public class StudyLogDeleteResponse {

    private String message;
    private Long deletedId;

    // 기본 생성자
    private StudyLogDeleteResponse() {
    }

    // 정적 팩토리 메서드
    public static StudyLogDeleteResponse of(Long id) {
        StudyLogDeleteResponse response = new StudyLogDeleteResponse();
        response.message = "학습 일지가 성공적으로 삭제되었습니다.";
        response.deletedId = id;
        return response;
    }

    // Getter 메서드
    public String getMessage() {
        return message;
    }

    public Long getDeletedId() {
        return deletedId;
    }
}
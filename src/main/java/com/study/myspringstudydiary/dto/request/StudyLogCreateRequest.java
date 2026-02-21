package com.study.myspringstudydiary.dto.request;

import com.study.myspringstudydiary.entity.StudyLog;
import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StudyLogCreateRequest {

    private String title;
    private String content;
    @Setter
    private String category;
    private String understanding;
    private Integer studyTime;
    private LocalDate studyDate;

}
package com.study.myspringstudydiary.global.mapper;

import com.study.myspringstudydiary.study_log.dto.request.StudyLogCreateRequest;
import com.study.myspringstudydiary.study_log.dto.request.StudyLogUpdateRequest;
import com.study.myspringstudydiary.study_log.dto.response.StudyLogResponse;
import com.study.myspringstudydiary.study_log.entity.StudyLog;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-05T19:56:20+0900",
    comments = "version: 1.6.3, compiler: javac, environment: Java 17.0.18 (Microsoft)"
)
@Component
public class StudyLogMapperImpl implements StudyLogMapper {

    @Override
    public StudyLogResponse toResponse(StudyLog studyLog) {
        if ( studyLog == null ) {
            return null;
        }

        StudyLogResponse.StudyLogResponseBuilder studyLogResponse = StudyLogResponse.builder();

        studyLogResponse.id( studyLog.getId() );
        studyLogResponse.title( studyLog.getTitle() );
        studyLogResponse.content( studyLog.getContent() );
        if ( studyLog.getCategory() != null ) {
            studyLogResponse.category( studyLog.getCategory().name() );
        }
        if ( studyLog.getUnderstanding() != null ) {
            studyLogResponse.understanding( studyLog.getUnderstanding().name() );
        }
        studyLogResponse.studyTime( studyLog.getStudyTime() );
        studyLogResponse.studyDate( studyLog.getStudyDate() );
        studyLogResponse.createdAt( studyLog.getCreatedAt() );
        studyLogResponse.updatedAt( studyLog.getUpdatedAt() );

        return studyLogResponse.build();
    }

    @Override
    public StudyLog toEntity(StudyLogCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        StudyLog.StudyLogBuilder studyLog = StudyLog.builder();

        studyLog.title( request.getTitle() );
        studyLog.content( request.getContent() );
        studyLog.studyTime( request.getStudyTime() );
        studyLog.studyDate( request.getStudyDate() );

        studyLog.category( com.study.myspringstudydiary.study_log.entity.Category.valueOf(request.getCategory()) );
        studyLog.understanding( com.study.myspringstudydiary.study_log.entity.Understanding.valueOf(request.getUnderstanding()) );

        return studyLog.build();
    }

    @Override
    public List<StudyLogResponse> toResponseList(List<StudyLog> studyLogs) {
        if ( studyLogs == null ) {
            return null;
        }

        List<StudyLogResponse> list = new ArrayList<StudyLogResponse>( studyLogs.size() );
        for ( StudyLog studyLog : studyLogs ) {
            list.add( toResponse( studyLog ) );
        }

        return list;
    }

    @Override
    public void updateEntityFromRequest(StudyLogUpdateRequest request, StudyLog studyLog) {
        if ( request == null ) {
            return;
        }

        studyLog.setTitle( request.getTitle() );
        studyLog.setContent( request.getContent() );
        studyLog.setStudyTime( request.getStudyTime() );
        studyLog.setStudyDate( request.getStudyDate() );

        studyLog.setCategory( request.getCategory() != null ? com.study.myspringstudydiary.study_log.entity.Category.valueOf(request.getCategory()) : studyLog.getCategory() );
        studyLog.setUnderstanding( request.getUnderstanding() != null ? com.study.myspringstudydiary.study_log.entity.Understanding.valueOf(request.getUnderstanding()) : studyLog.getUnderstanding() );
    }
}

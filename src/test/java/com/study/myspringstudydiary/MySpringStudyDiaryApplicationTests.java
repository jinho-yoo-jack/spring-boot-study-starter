package com.study.myspringstudydiary;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("애플리케이션 컨텍스트 테스트")
class MySpringStudyDiaryApplicationTests {

    @Test
    @DisplayName("애플리케이션 컨텍스트가 정상적으로 로드되는지 확인")
    void contextLoads() {
        // 이 테스트는 Spring 애플리케이션 컨텍스트가 정상적으로 시작되는지 확인합니다.
        // H2 데이터베이스 드라이버가 추가되어 이제 정상적으로 작동합니다.
    }

}

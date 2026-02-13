package com.study.myspringstudydiary;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("애플리케이션 컨텍스트 로드 테스트")
class MySpringStudyDiaryApplicationTests {

    @Test
    @DisplayName("Spring Application Context가 정상적으로 로드되는지 확인")
    void contextLoads() {
        // Spring Application Context가 정상적으로 로드되는지 확인
        // 이 테스트가 성공하면 기본적인 Spring Boot 설정이 올바르게 되어 있음을 의미
    }

}

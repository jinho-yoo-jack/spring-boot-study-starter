package com.study.myspringstudydiary.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA Configuration
 * 강의 교안 예제에 따른 JPA 설정
 */
@Configuration
@EnableJpaRepositories(basePackages = {
    "com.study.myspringstudydiary.study_log.repository",
    "com.study.myspringstudydiary.auth.repository"
})
@EnableJpaAuditing
@EnableTransactionManagement
public class JpaConfig {
    // JPA 관련 추가 설정이 필요한 경우 여기에 Bean을 정의합니다.
}
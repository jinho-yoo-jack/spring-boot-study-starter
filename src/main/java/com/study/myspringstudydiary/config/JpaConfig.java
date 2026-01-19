package com.study.myspringstudydiary.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // JPA Auditing 활성화를 위한 설정 클래스
    // @CreatedDate, @LastModifiedDate 어노테이션이 동작하도록 함
}
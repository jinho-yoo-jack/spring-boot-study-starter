package com.study.myspringstudydiary.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * RestClient 설정 클래스
 * Spring 6.1+ (Spring Boot 3.2+)의 새로운 HTTP 클라이언트 설정
 *
 * RestClient는 RestTemplate의 후계자로 Fluent API를 제공
 */
@Configuration
public class RestClientConfig {

    /**
     * Discord Webhook용 RestClient Bean
     *
     * Discord API는 JSON 형식으로 통신하며,
     * 별도의 인증 헤더가 필요 없음 (Webhook URL 자체가 인증 역할)
     *
     * @return Discord 전용 RestClient
     */
    @Bean(name = "discordRestClient")
    public RestClient discordRestClient() {
        // 타임아웃 설정을 위한 Factory
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));   // 연결 타임아웃: 5초
        factory.setReadTimeout(Duration.ofSeconds(10));      // 읽기 타임아웃: 10초

        return RestClient.builder()
                // Discord Webhook은 URL이 동적이므로 baseUrl 설정 안함
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "StudyLogBot/1.0")
                .requestFactory(factory)
                .build();
    }

    /**
     * 범용 RestClient Bean (향후 다른 API 연동 시 사용)
     *
     * @return 범용 RestClient
     */
    @Bean(name = "generalRestClient")
    public RestClient generalRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));

        return RestClient.builder()
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .requestFactory(factory)
                .build();
    }
}
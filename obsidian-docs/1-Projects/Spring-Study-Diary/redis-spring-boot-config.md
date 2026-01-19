# Spring Boot Redis 설정 (with Password)

## 📋 Quick Setup Guide

### 1. Dependencies 추가 (build.gradle)

```gradle
dependencies {
    // Spring Boot Redis Starter
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'

    // Lettuce (기본 Redis 클라이언트)
    implementation 'io.lettuce:lettuce-core'

    // Redis Session (선택사항)
    implementation 'org.springframework.session:spring-session-data-redis'
}
```

### 2. Docker Compose 설정

```yaml
# docker-compose.yml
version: '3.8'

services:
  mysql:
    # ... existing mysql config ...

  redis:
    image: redis:7-alpine
    container_name: diary-redis
    restart: unless-stopped
    ports:
      - "6379:6379"
    command: redis-server --requirepass ${REDIS_PASSWORD} --appendonly yes
    environment:
      - REDIS_PASSWORD=${REDIS_PASSWORD}
    volumes:
      - redis-data:/data
    networks:
      - diary-network

volumes:
  mysql-data:
    driver: local
  redis-data:
    driver: local

networks:
  diary-network:
    driver: bridge
```

### 3. 환경 변수 추가 (.env)

```env
# 기존 MySQL 설정들...

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=MySecureRedisPassword123!@#
REDIS_DATABASE=0
REDIS_TIMEOUT=2000
```

### 4. Application 설정

```yaml
# application.yml
spring:
  # 기존 설정들...

  # Redis 설정
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD}
    database: ${REDIS_DATABASE:0}
    timeout: ${REDIS_TIMEOUT:2000}ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms
      shutdown-timeout: 100ms

  # Redis Session 설정 (선택사항)
  session:
    store-type: redis
    redis:
      namespace: spring:session
      flush-mode: on_save
```

### 5. Redis Configuration 클래스

```java
package com.study.myspringstudydiary.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.password}")
    private String redisPassword;

    @Value("${spring.redis.database}")
    private int database;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig =
            new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);
        redisConfig.setPassword(redisPassword);
        redisConfig.setDatabase(database);

        return new LettuceConnectionFactory(redisConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());

        // Key Serializer
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value Serializer
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))  // 캐시 만료 시간
            .disableCachingNullValues();       // null 값 캐싱 비활성화

        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(cacheConfig)
            .build();
    }
}
```

### 6. Redis Service 예제

```java
package com.study.myspringstudydiary.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    // 데이터 저장
    public void set(String key, Object value) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(key, value);
        log.debug("Saved to Redis - key: {}, value: {}", key, value);
    }

    // 데이터 저장 (만료시간 설정)
    public void setWithExpire(String key, Object value, long timeout) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(key, value, Duration.ofSeconds(timeout));
        log.debug("Saved to Redis with TTL - key: {}, value: {}, timeout: {}s",
            key, value, timeout);
    }

    // 데이터 조회
    public Object get(String key) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        Object value = values.get(key);
        log.debug("Retrieved from Redis - key: {}, value: {}", key, value);
        return value;
    }

    // 데이터 삭제
    public boolean delete(String key) {
        Boolean result = redisTemplate.delete(key);
        log.debug("Deleted from Redis - key: {}, result: {}", key, result);
        return Boolean.TRUE.equals(result);
    }

    // 키 존재 여부 확인
    public boolean hasKey(String key) {
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }
}
```

### 7. Cache 적용 예제

```java
package com.study.myspringstudydiary.service;

import com.study.myspringstudydiary.dto.UserResponse;
import com.study.myspringstudydiary.entity.User;
import com.study.myspringstudydiary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CachedUserService {

    private final UserRepository userRepository;

    @Cacheable(value = "users", key = "#id")
    public UserResponse getUserById(Long id) {
        log.info("Fetching user from DB - id: {}", id);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserResponse.from(user);
    }

    @CachePut(value = "users", key = "#id")
    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        log.info("Updating user and cache - id: {}", id);
        // 업데이트 로직...
        return updatedUserResponse;
    }

    @CacheEvict(value = "users", key = "#id")
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user and clearing cache - id: {}", id);
        userRepository.deleteById(id);
    }

    @CacheEvict(value = "users", allEntries = true)
    public void clearAllUserCache() {
        log.info("Clearing all user cache");
    }
}
```

### 8. 테스트 코드

```java
package com.study.myspringstudydiary.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RedisServiceTest {

    @Autowired
    private RedisService redisService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void testRedisConnection() {
        // Given
        String key = "test:key";
        String value = "test-value";

        // When
        redisService.set(key, value);
        Object retrieved = redisService.get(key);

        // Then
        assertThat(retrieved).isEqualTo(value);

        // Cleanup
        redisService.delete(key);
    }

    @Test
    void testRedisExpiration() throws InterruptedException {
        // Given
        String key = "test:expire";
        String value = "expires-soon";

        // When
        redisService.setWithExpire(key, value, 1); // 1초 후 만료

        // Then
        assertThat(redisService.get(key)).isEqualTo(value);

        Thread.sleep(1100); // 1.1초 대기

        assertThat(redisService.hasKey(key)).isFalse();
    }
}
```

---

## 🔧 운영 명령어

### Redis CLI 명령어

```bash
# Redis 접속
redis-cli -a YourPassword

# 모든 키 조회 (주의: 프로덕션에서는 사용 자제)
KEYS *

# 특정 패턴의 키 조회
KEYS user:*

# 키 TTL 확인
TTL key_name

# 캐시 통계 확인
INFO stats

# 메모리 사용량 확인
INFO memory

# 모든 캐시 삭제 (주의!)
FLUSHDB
```

### Spring Boot Actuator 설정

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health, metrics, caches
  health:
    redis:
      enabled: true
```

---

## 📊 모니터링

### Redis 메트릭 수집

```java
@Component
public class RedisMetrics {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Scheduled(fixedDelay = 60000) // 1분마다
    public void collectMetrics() {
        Properties info = redisTemplate.getConnectionFactory()
            .getConnection()
            .info();

        log.info("Redis Metrics - Used Memory: {}, Connected Clients: {}",
            info.getProperty("used_memory_human"),
            info.getProperty("connected_clients"));
    }
}
```

---

## 🚨 트러블슈팅

### 1. Connection Refused
```
원인: Redis 서버가 실행되지 않았거나 네트워크 문제
해결: docker-compose up -d redis
```

### 2. NOAUTH Authentication Required
```
원인: 패스워드가 설정되었지만 제공되지 않음
해결: application.yml에 spring.redis.password 확인
```

### 3. Serialization Error
```
원인: 객체 직렬화 실패
해결: Serializable 인터페이스 구현 또는 JSON Serializer 사용
```

---

## 📚 참고 자료

- [Spring Data Redis Reference](https://docs.spring.io/spring-data/redis/docs/current/reference/html/)
- [Lettuce Client Documentation](https://lettuce.io/docs/)
- [Redis Commands Reference](https://redis.io/commands)

---

*작성일: 2026-01-19*
*프로젝트: my-spring-study-diary*
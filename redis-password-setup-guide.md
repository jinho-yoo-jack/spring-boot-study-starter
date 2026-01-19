# Redis 패스워드 설정 가이드 (Linux)

> 작성일: 2026-01-19
> 주제: Redis 서버 보안 - 패스워드 인증 설정

## 📋 목차

1. [Redis 패스워드 설정의 필요성](#redis-패스워드-설정의-필요성)
2. [설정 방법](#설정-방법)
3. [클라이언트 접속 방법](#클라이언트-접속-방법)
4. [Spring Boot 연동](#spring-boot-연동)
5. [보안 권장사항](#보안-권장사항)
6. [트러블슈팅](#트러블슈팅)

---

## Redis 패스워드 설정의 필요성

Redis는 기본적으로 패스워드 없이 동작하도록 설정되어 있습니다. 이는 개발 환경에서는 편리하지만, 프로덕션 환경에서는 심각한 보안 위협이 될 수 있습니다.

### 주요 보안 위험
- **무단 접근**: 누구나 Redis 서버에 접근 가능
- **데이터 유출**: 캐시된 민감한 정보 노출
- **서비스 거부 공격**: FLUSHALL 등 위험한 명령 실행 가능

---

## 설정 방법

### 1. Redis 설치 확인

```bash
# Redis 버전 확인
redis-server --version

# Redis 서비스 상태 확인
sudo systemctl status redis
# 또는
sudo service redis-server status
```

### 2. Redis 설정 파일 위치 확인

Redis 설정 파일은 일반적으로 다음 경로에 있습니다:

```bash
# Ubuntu/Debian
/etc/redis/redis.conf

# CentOS/RHEL/Fedora
/etc/redis.conf

# 설정 파일 찾기
sudo find /etc -name "redis.conf" 2>/dev/null
```

### 3. 패스워드 설정 (redis.conf 수정)

#### 방법 1: 설정 파일 직접 편집

```bash
# 설정 파일 백업
sudo cp /etc/redis/redis.conf /etc/redis/redis.conf.backup

# 설정 파일 편집
sudo nano /etc/redis/redis.conf
# 또는
sudo vim /etc/redis/redis.conf
```

다음 라인을 찾아서 수정합니다:

```conf
# requirepass 라인을 찾아서 주석 해제 및 패스워드 설정
# 변경 전:
# requirepass foobared

# 변경 후:
requirepass YourStrongPasswordHere123!@#
```

#### 방법 2: sed 명령어로 자동 수정

```bash
# 패스워드 설정 (기존 requirepass 라인이 있는 경우)
sudo sed -i 's/^# requirepass .*/requirepass YourStrongPasswordHere123!@#/' /etc/redis/redis.conf

# 패스워드 설정 (requirepass 라인이 없는 경우 추가)
echo "requirepass YourStrongPasswordHere123!@#" | sudo tee -a /etc/redis/redis.conf
```

### 4. 추가 보안 설정

```conf
# bind 설정 (특정 IP만 접근 허용)
bind 127.0.0.1 ::1
# 모든 IP에서 접근 허용 시 (보안 주의!)
# bind 0.0.0.0

# protected mode 활성화
protected-mode yes

# 위험한 명령어 비활성화
rename-command FLUSHDB ""
rename-command FLUSHALL ""
rename-command KEYS ""
rename-command CONFIG "CONFIG_akdlqkrwl293"  # 또는 랜덤 문자열로 변경
```

### 5. Redis 서비스 재시작

```bash
# systemd 사용 시
sudo systemctl restart redis
sudo systemctl status redis

# service 명령 사용 시
sudo service redis-server restart
sudo service redis-server status

# Docker 사용 시
docker restart redis-container-name
```

### 6. 패스워드 설정 확인

```bash
# Redis CLI로 접속 시도
redis-cli

# 패스워드 없이 명령 실행 시도 (실패해야 정상)
127.0.0.1:6379> ping
(error) NOAUTH Authentication required.

# 인증
127.0.0.1:6379> AUTH YourStrongPasswordHere123!@#
OK

# 이제 명령 실행 가능
127.0.0.1:6379> ping
PONG
```

---

## 클라이언트 접속 방법

### 1. Redis CLI 접속

```bash
# 방법 1: 접속 시 패스워드 지정
redis-cli -a YourStrongPasswordHere123!@#

# 방법 2: 환경변수 사용 (보안 권장)
export REDISCLI_AUTH=YourStrongPasswordHere123!@#
redis-cli

# 방법 3: 접속 후 인증
redis-cli
127.0.0.1:6379> AUTH YourStrongPasswordHere123!@#
```

### 2. 원격 접속

```bash
# 원격 서버 접속
redis-cli -h redis.example.com -p 6379 -a YourStrongPasswordHere123!@#

# SSL/TLS 사용 시
redis-cli -h redis.example.com -p 6379 -a YourStrongPasswordHere123!@# --tls \
    --cert /path/to/client-cert.pem \
    --key /path/to/client-key.pem \
    --cacert /path/to/ca-cert.pem
```

---

## Spring Boot 연동

### 1. application.yml 설정

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: YourStrongPasswordHere123!@#
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms
```

### 2. 환경변수 사용 (권장)

```yaml
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD}
    timeout: ${REDIS_TIMEOUT:2000ms}
```

`.env` 파일:
```env
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=YourStrongPasswordHere123!@#
REDIS_TIMEOUT=2000ms
```

### 3. Java Configuration

```java
@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.password}")
    private String redisPassword;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig =
            new RedisStandaloneConfiguration(redisHost, redisPort);
        redisConfig.setPassword(redisPassword);

        return new LettuceConnectionFactory(redisConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}
```

---

## Docker Compose 설정

```yaml
version: '3.8'

services:
  redis:
    image: redis:7-alpine
    container_name: redis-secure
    restart: unless-stopped
    ports:
      - "6379:6379"
    command: redis-server --requirepass ${REDIS_PASSWORD} --appendonly yes
    environment:
      - REDIS_PASSWORD=${REDIS_PASSWORD}
    volumes:
      - redis-data:/data
      - ./redis.conf:/usr/local/etc/redis/redis.conf
    networks:
      - app-network

volumes:
  redis-data:
    driver: local

networks:
  app-network:
    driver: bridge
```

---

## 보안 권장사항

### 1. 강력한 패스워드 사용

```bash
# 랜덤 패스워드 생성
openssl rand -base64 32
# 또는
pwgen -s 32 1
```

### 2. ACL (Access Control List) 설정 (Redis 6.0+)

```conf
# redis.conf
aclfile /etc/redis/users.acl
```

users.acl 파일:
```acl
# 기본 사용자 비활성화
user default on nopass ~* &* +@all

# 애플리케이션 사용자
user app-user on >app_password_here ~* &* +@all -@dangerous

# 읽기 전용 사용자
user readonly on >readonly_password ~* &* +@read
```

### 3. 네트워크 보안

```bash
# 방화벽 설정 (UFW)
sudo ufw allow from 192.168.1.0/24 to any port 6379
sudo ufw deny 6379

# iptables
sudo iptables -A INPUT -p tcp --dport 6379 -s 192.168.1.0/24 -j ACCEPT
sudo iptables -A INPUT -p tcp --dport 6379 -j DROP
```

### 4. 로그 모니터링

```conf
# redis.conf
logfile /var/log/redis/redis-server.log
loglevel notice
```

로그 확인:
```bash
sudo tail -f /var/log/redis/redis-server.log
```

---

## 트러블슈팅

### 문제 1: 패스워드 설정 후 접속 불가

**증상**: `NOAUTH Authentication required` 에러

**해결**:
```bash
# Redis 서비스 재시작
sudo systemctl restart redis

# 설정 파일 문법 확인
redis-server /etc/redis/redis.conf --test-memory 1
```

### 문제 2: Spring Boot 연결 실패

**증상**: `RedisConnectionFailureException`

**해결**:
1. Redis 서버 상태 확인
2. 방화벽 설정 확인
3. bind 설정 확인
4. 패스워드 확인

```java
// 연결 테스트 코드
@Component
public class RedisConnectionTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    public void testConnection() {
        try {
            redisTemplate.opsForValue().set("test", "value");
            String value = (String) redisTemplate.opsForValue().get("test");
            log.info("Redis connection successful: {}", value);
        } catch (Exception e) {
            log.error("Redis connection failed", e);
        }
    }
}
```

### 문제 3: 패스워드 분실

**해결**:
```bash
# Redis 서비스 중지
sudo systemctl stop redis

# 패스워드 없이 임시 실행
redis-server --port 6380

# 다른 터미널에서 접속
redis-cli -p 6380

# 새 패스워드 설정
127.0.0.1:6380> CONFIG SET requirepass "NewPassword123!@#"

# 설정 파일 수정
sudo sed -i 's/requirepass .*/requirepass NewPassword123!@#/' /etc/redis/redis.conf

# Redis 정상 시작
sudo systemctl start redis
```

---

## 패스워드 관리 모범 사례

### 1. 환경별 패스워드 분리

```yaml
# application-dev.yml
spring:
  redis:
    password: ${REDIS_PASSWORD_DEV}

# application-prod.yml
spring:
  redis:
    password: ${REDIS_PASSWORD_PROD}
```

### 2. 시크릿 관리 도구 사용

- **HashiCorp Vault**
- **AWS Secrets Manager**
- **Kubernetes Secrets**
- **Azure Key Vault**

### 3. 정기적인 패스워드 변경

```bash
#!/bin/bash
# rotate-redis-password.sh

NEW_PASSWORD=$(openssl rand -base64 32)
OLD_PASSWORD=${REDIS_PASSWORD}

# Redis 패스워드 변경
redis-cli -a ${OLD_PASSWORD} CONFIG SET requirepass "${NEW_PASSWORD}"

# 설정 파일 업데이트
sudo sed -i "s/requirepass ${OLD_PASSWORD}/requirepass ${NEW_PASSWORD}/" /etc/redis/redis.conf

# 환경변수 업데이트
echo "REDIS_PASSWORD=${NEW_PASSWORD}" > /etc/redis/.env

echo "Password rotated successfully"
```

---

## 참고 자료

- [Redis Security Official Documentation](https://redis.io/docs/manual/security/)
- [Redis ACL Documentation](https://redis.io/docs/manual/security/acl/)
- [Spring Data Redis Reference](https://docs.spring.io/spring-data/redis/docs/current/reference/html/)
- [OWASP Redis Security Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Redis_Security_Cheat_Sheet.html)

---

## 체크리스트

- [ ] Redis 설치 및 버전 확인
- [ ] redis.conf 백업 생성
- [ ] 강력한 패스워드 설정 (최소 16자, 특수문자 포함)
- [ ] bind 설정으로 접근 IP 제한
- [ ] protected-mode 활성화
- [ ] 위험한 명령어 비활성화 또는 이름 변경
- [ ] 방화벽 설정
- [ ] 로그 설정 및 모니터링
- [ ] 애플리케이션 연동 테스트
- [ ] 패스워드 관리 정책 수립
- [ ] 정기적인 보안 감사 일정 계획

---

*작성자: Claude*
*최종 수정: 2026-01-19*
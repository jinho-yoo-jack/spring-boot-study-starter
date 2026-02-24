# Discord 알림 기능 설정 가이드

## 개요
StudyLog 생성 시 Discord 채널에 자동으로 알림을 전송하는 기능입니다.
Spring Boot의 RestClient를 사용하여 Discord Webhook API와 통신합니다.

## 주요 기능
- ✨ 새로운 학습 일지 작성 시 Discord 알림
- 📝 학습 일지 수정 시 Discord 알림
- 🗑️ 학습 일지 삭제 시 Discord 알림
- 🎯 비동기 처리로 메인 로직에 영향 없음

## Discord Webhook 설정 방법

### 1. Discord 서버에서 Webhook 생성
1. Discord 서버 설정 → 연동 → 웹후크
2. "새 웹후크" 버튼 클릭
3. 웹후크 이름 설정 (예: Study Log Bot)
4. 알림을 받을 채널 선택
5. "웹후크 URL 복사" 클릭

### 2. 애플리케이션 설정

#### 방법 1: application.yml 수정
```yaml
discord:
  webhook:
    url: https://discord.com/api/webhooks/YOUR_WEBHOOK_ID/YOUR_WEBHOOK_TOKEN
    enabled: true
    username: Study Log Bot
    avatar-url: https://i.imgur.com/AfFp7pu.png
```

#### 방법 2: 환경 변수 설정 (권장)
```bash
# .env 파일 또는 시스템 환경 변수
export DISCORD_WEBHOOK_URL=https://discord.com/api/webhooks/YOUR_WEBHOOK_ID/YOUR_WEBHOOK_TOKEN
export DISCORD_WEBHOOK_ENABLED=true
```

### 3. IntelliJ IDEA에서 환경 변수 설정
1. Run/Debug Configurations 열기
2. Environment variables 섹션에 추가:
   - `DISCORD_WEBHOOK_URL`: 복사한 Webhook URL
   - `DISCORD_WEBHOOK_ENABLED`: true

## 테스트 방법

### 1. 테스트 엔드포인트 사용
```bash
curl -X POST http://localhost:8081/api/discord/test
```

### 2. Swagger UI 사용
1. http://localhost:8081/swagger-ui.html 접속
2. Discord 섹션 → /api/discord/test → Try it out → Execute

### 3. StudyLog 생성
StudyLog를 생성하면 자동으로 Discord 알림이 전송됩니다.

```bash
curl -X POST http://localhost:8081/api/study-logs \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Spring Boot 외부 API 통신 학습",
    "content": "RestClient를 사용한 Discord 연동 구현",
    "category": "SPRING",
    "understanding": "HIGH",
    "studyTime": 120,
    "studyDate": "2024-01-20"
  }'
```

## Discord 메시지 형식

### 생성 알림
```
✨ New Study Log
━━━━━━━━━━━━━━━━━━━
📚 제목: Spring Boot 외부 API 통신 학습
📂 카테고리: 🌱 SPRING | 💡 이해도: 😊 HIGH
⏱️ 학습 시간: 120분 | 📅 학습 날짜: 2024-01-20
📝 내용: RestClient를 사용한 Discord 연동 구현
━━━━━━━━━━━━━━━━━━━
✨ New Study Log | ID: 1
```

## 구현 세부사항

### 사용 기술
- **Spring Boot 3.2+**: 최신 RestClient 사용
- **비동기 처리**: @Async를 사용하여 Discord 알림을 비동기로 전송
- **에러 처리**: Discord API 실패가 메인 로직에 영향을 주지 않도록 격리

### 주요 클래스
- `DiscordNotificationService`: Discord 알림 전송 서비스
- `DiscordWebhookMessage`: Discord Webhook API 메시지 DTO
- `RestClientConfig`: RestClient Bean 설정
- `AsyncConfig`: 비동기 처리 설정

### 타임아웃 설정
- 연결 타임아웃: 5초
- 읽기 타임아웃: 10초

## 트러블슈팅

### Discord 알림이 오지 않는 경우
1. `DISCORD_WEBHOOK_ENABLED`가 `true`로 설정되어 있는지 확인
2. Webhook URL이 올바른지 확인
3. 로그 확인: `grep "Discord" logs/study-diary.log`

### 429 Too Many Requests 에러
- Discord API Rate Limit에 걸린 경우
- 해결방법: 알림 전송 간격 조절 필요

### Connection Timeout 에러
- Discord 서버 연결 문제
- 해결방법: 네트워크 상태 확인, 타임아웃 시간 증가

## 보안 주의사항
⚠️ **Webhook URL을 절대 공개 저장소에 커밋하지 마세요!**
- `.gitignore`에 `.env` 파일 추가
- 환경 변수나 시크릿 관리 도구 사용
- 프로덕션 환경에서는 별도의 Webhook URL 사용

## 향후 개선사항
- [ ] 멘션(@everyone, @here) 기능 추가
- [ ] 학습 통계 요약 알림 (일간/주간)
- [ ] 카테고리별 다른 채널로 알림 전송
- [ ] 리치 Embed 메시지 커스터마이징
- [ ] Rate Limiting 처리
- [ ] Webhook URL 다중 설정 지원
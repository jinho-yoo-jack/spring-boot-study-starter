package com.study.myspringstudydiary.discord.service;

import com.study.myspringstudydiary.discord.dto.DiscordWebhookMessage;
import com.study.myspringstudydiary.discord.dto.DiscordWebhookMessage.*;
import com.study.myspringstudydiary.study_log.entity.StudyLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Discord 알림 서비스
 *
 * StudyLog 생성/수정/삭제 시 Discord 채널에 알림을 전송
 * RestClient를 사용하여 Discord Webhook API와 통신
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordNotificationService {

    @Qualifier("discordRestClient")
    private final RestClient discordRestClient;

    @Value("${discord.webhook.url}")
    private String webhookUrl;

    @Value("${discord.webhook.enabled}")
    private boolean webhookEnabled;

    @Value("${discord.webhook.username}")
    private String botUsername;

    @Value("${discord.webhook.avatar-url}")
    private String avatarUrl;

    // Discord Embed 색상 코드 (16진수)
    private static final int COLOR_SUCCESS = 0x00FF00;  // 녹색
    private static final int COLOR_INFO = 0x3498DB;     // 파란색
    private static final int COLOR_WARNING = 0xFFD700;  // 금색
    private static final int COLOR_ERROR = 0xFF0000;    // 빨간색

    /**
     * 새로운 StudyLog 생성 알림
     *
     * @Async를 사용하여 비동기로 처리 (메인 로직을 블로킹하지 않음)
     *
     * @param studyLog 생성된 StudyLog 엔티티
     */
    @Async
    public void sendStudyLogCreatedNotification(StudyLog studyLog) {
        if (!webhookEnabled) {
            log.debug("Discord webhook is disabled. Skipping notification.");
            return;
        }

        try {
            DiscordWebhookMessage message = createStudyLogEmbed(
                    studyLog,
                    "새로운 학습 일지가 작성되었습니다!",
                    COLOR_SUCCESS,
                    "✨ New Study Log"
            );

            sendWebhookMessage(message);
            log.info("Successfully sent Discord notification for StudyLog ID: {}", studyLog.getId());

        } catch (Exception e) {
            // Discord 알림 실패가 메인 비즈니스 로직에 영향을 주면 안됨
            log.error("Failed to send Discord notification for StudyLog ID: {}", studyLog.getId(), e);
        }
    }

    /**
     * StudyLog 수정 알림
     *
     * @param studyLog 수정된 StudyLog 엔티티
     */
    @Async
    public void sendStudyLogUpdatedNotification(StudyLog studyLog) {
        if (!webhookEnabled) {
            return;
        }

        try {
            DiscordWebhookMessage message = createStudyLogEmbed(
                    studyLog,
                    "학습 일지가 수정되었습니다.",
                    COLOR_INFO,
                    "📝 Updated Study Log"
            );

            sendWebhookMessage(message);

        } catch (Exception e) {
            log.error("Failed to send Discord update notification for StudyLog ID: {}", studyLog.getId(), e);
        }
    }

    /**
     * StudyLog 삭제 알림
     *
     * @param studyLogId 삭제된 StudyLog ID
     * @param title 삭제된 StudyLog 제목
     */
    @Async
    public void sendStudyLogDeletedNotification(Long studyLogId, String title) {
        if (!webhookEnabled) {
            return;
        }

        try {
            DiscordWebhookMessage message = DiscordWebhookMessage.builder()
                    .username(botUsername)
                    .avatarUrl(avatarUrl)
                    .embeds(List.of(
                            Embed.builder()
                                    .title("🗑️ Study Log Deleted")
                                    .description("학습 일지가 삭제되었습니다.")
                                    .color(COLOR_WARNING)
                                    .fields(List.of(
                                            Field.builder()
                                                    .name("ID")
                                                    .value(String.valueOf(studyLogId))
                                                    .inline(true)
                                                    .build(),
                                            Field.builder()
                                                    .name("제목")
                                                    .value(title != null ? title : "N/A")
                                                    .inline(true)
                                                    .build()
                                    ))
                                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                    .build()
                    ))
                    .build();

            sendWebhookMessage(message);

        } catch (Exception e) {
            log.error("Failed to send Discord delete notification for StudyLog ID: {}", studyLogId, e);
        }
    }

    /**
     * StudyLog를 Discord Embed 메시지로 변환
     *
     * @param studyLog StudyLog 엔티티
     * @param title Embed 제목
     * @param color Embed 색상
     * @param footerText 푸터 텍스트
     * @return Discord 메시지 객체
     */
    private DiscordWebhookMessage createStudyLogEmbed(StudyLog studyLog, String title, int color, String footerText) {
        List<Field> fields = new ArrayList<>();

        // 제목 필드
        fields.add(Field.builder()
                .name("📚 제목")
                .value(studyLog.getTitle())
                .inline(false)
                .build());

        // 카테고리와 이해도를 한 줄에
        fields.add(Field.builder()
                .name("📂 카테고리")
                .value(studyLog.getCategory().getIcon() + " " + studyLog.getCategory().name())
                .inline(true)
                .build());

        fields.add(Field.builder()
                .name("💡 이해도")
                .value(studyLog.getUnderstanding().getEmoji() + " " + studyLog.getUnderstanding().name())
                .inline(true)
                .build());

        // 학습 시간
        fields.add(Field.builder()
                .name("⏱️ 학습 시간")
                .value(studyLog.getStudyTime() + "분")
                .inline(true)
                .build());

        // 학습 날짜
        fields.add(Field.builder()
                .name("📅 학습 날짜")
                .value(studyLog.getStudyDate().toString())
                .inline(true)
                .build());

        // 내용 (최대 1000자로 제한)
        String content = studyLog.getContent();
        if (content != null && content.length() > 1000) {
            content = content.substring(0, 997) + "...";
        }
        fields.add(Field.builder()
                .name("📝 내용")
                .value(content != null ? content : "내용 없음")
                .inline(false)
                .build());

        // Embed 생성
        Embed embed = Embed.builder()
                .title(title)
                .color(color)
                .fields(fields)
                .footer(Footer.builder()
                        .text(footerText + " | ID: " + studyLog.getId())
                        .build())
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();

        // 메시지 생성
        return DiscordWebhookMessage.builder()
                .username(botUsername)
                .avatarUrl(avatarUrl)
                .embeds(List.of(embed))
                .build();
    }

    /**
     * Discord Webhook으로 메시지 전송
     *
     * @param message 전송할 메시지
     */
    private void sendWebhookMessage(DiscordWebhookMessage message) {
        try {
            discordRestClient.post()
                    .uri(webhookUrl)
                    .body(message)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        log.error("Discord API Client Error: {} - {}",
                                response.getStatusCode(), response.getStatusText());
                        throw new RestClientException("Discord API 요청 실패: " + response.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        log.error("Discord API Server Error: {} - {}",
                                response.getStatusCode(), response.getStatusText());
                        throw new RestClientException("Discord 서버 오류: " + response.getStatusCode());
                    })
                    .toBodilessEntity(); // Discord Webhook은 응답 본문이 없음

            log.debug("Discord webhook message sent successfully");

        } catch (RestClientException e) {
            log.error("Failed to send Discord webhook message", e);
            throw e;
        }
    }

    /**
     * 테스트용 알림 전송
     *
     * @return 전송 성공 여부
     */
    public boolean sendTestNotification() {
        if (!webhookEnabled) {
            log.info("Discord webhook is disabled");
            return false;
        }

        try {
            DiscordWebhookMessage message = DiscordWebhookMessage.builder()
                    .username(botUsername)
                    .avatarUrl(avatarUrl)
                    .content("🎉 Discord 연동 테스트가 성공했습니다! Study Log Bot이 정상적으로 작동합니다.")
                    .build();

            sendWebhookMessage(message);
            return true;

        } catch (Exception e) {
            log.error("Test notification failed", e);
            return false;
        }
    }
}
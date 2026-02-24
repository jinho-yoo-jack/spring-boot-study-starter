package com.study.myspringstudydiary.controller;

import com.study.myspringstudydiary.service.DiscordNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Discord 알림 테스트 컨트롤러
 *
 * Discord Webhook 연동 테스트를 위한 엔드포인트 제공
 */
@Tag(name = "Discord", description = "Discord 알림 테스트 API")
@RestController
@RequestMapping("/api/discord")
@RequiredArgsConstructor
public class DiscordTestController {

    private final DiscordNotificationService discordNotificationService;

    /**
     * Discord 연동 테스트
     *
     * @return 테스트 성공/실패 메시지
     */
    @Operation(
            summary = "Discord 알림 테스트",
            description = "Discord Webhook 연동이 정상적으로 작동하는지 테스트합니다."
    )
    @PostMapping("/test")
    public ResponseEntity<String> testDiscordNotification() {
        boolean success = discordNotificationService.sendTestNotification();

        if (success) {
            return ResponseEntity.ok("Discord 알림 테스트가 성공했습니다! Discord 채널을 확인해주세요.");
        } else {
            return ResponseEntity.internalServerError()
                    .body("Discord 알림 테스트가 실패했습니다. 설정을 확인해주세요.");
        }
    }
}
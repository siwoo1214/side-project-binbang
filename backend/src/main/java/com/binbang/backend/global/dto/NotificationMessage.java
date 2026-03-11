package com.binbang.backend.global.dto;

/**
 * 앱 내 알림용 메시지 DTO
 * - RabbitMQ를 통해 전송됨
 * - WebSocket으로 실시간 알림 전송용
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {

    // 알림 유형
    private NotificationType notificationType;
    // 수신자 회원 아이디
    private Long memberId;
    // 알림 제목
    private String title;
    // 알림 내용
    private String content;
    // 관련 예약 아이디
    private Long reservationId;
    // 관련 숙소 아이디
    private Long accommodationId;

    // ㅏㅇㄹ림 생성 시각
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 알림 유형 Enum
     */
    public enum NotificationType {
        NEW_RESERVATION,      // 새 예약 도착 (호스트용)
        RESERVATION_CONFIRMED, // 예약 확정 (게스트용)
        RESERVATION_CANCELLED, // 예약 취소
        RESERVATION_COMPLETED  // 예약 완료
    }

}

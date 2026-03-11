package com.binbang.backend.global.service;

import com.binbang.backend.global.config.RabbitMQConfig;
import com.binbang.backend.global.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * 알림 메시지 소비(Consume) 서비스
 * - notification.queue에서 메시지를 받아서 처리
 * - 앱 내 알림, WebSocket 전송 등
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    // WebSocket 서비스 주입
    private final WebSocketService webSocketService;

    /**
     * 알림 Queue 리스너
     * - notification.queue에서 메시지를 받아서 처리
     *
     * @param notificationMessage 알림 정보
     */
    @RabbitListener(queues= RabbitMQConfig.NOTIFICATION_QUEUE)
    public void consumeNotificationMessage(NotificationMessage notificationMessage){
        try{
            log.info("알림 메시지 수신: type={}, memberId={}",
                    notificationMessage.getNotificationType(),
                    notificationMessage.getMemberId());

            // 알람 타입에 따라 분기 처리
            switch(notificationMessage.getNotificationType()){
                case NEW_RESERVATION:
                    // 새 예약 도착 알림(호스트용)
                    sendNewReservationNotification(notificationMessage);
                    break;

                case RESERVATION_CONFIRMED:
                    // 예약 확정 알림 (게스트용)
                    sendReservationConfirmedNotification(notificationMessage);
                    break;

                case RESERVATION_CANCELLED:
                    // 예약 취소 알림
                    sendReservationCancelledNotification(notificationMessage);
                    break;

                case RESERVATION_COMPLETED:
                    // 예약 완료 알림
                    sendReservationCompletedNotification(notificationMessage);
                    break;

                default:
                    log.warn("알 수 없는 알림 타입: {}", notificationMessage.getNotificationType());
            }
            log.info("알림 발송 완료: type={}, memberId={}",
                    notificationMessage.getNotificationType(),
                    notificationMessage.getMemberId());
        } catch (RuntimeException e) {
            log.error("알림 발송 실패: type={}, memberId={}, error={}",
                    notificationMessage.getNotificationType(),
                    notificationMessage.getMemberId(),
                    e.getMessage(), e);

            // 예외를 다시 던지면 쟈시도 메커니즘 작동
            throw new RuntimeException("알람 발송 실패",e);
        }
    }

    /**
     * 새 예약 도착 알림 (호스트용)
     */
    private void sendNewReservationNotification(NotificationMessage message) {
        // WebSocket으로 실시간 알림 전송
        webSocketService.sendNotification(message.getMemberId(), message);

        log.info("새 예약 알림: memberId={}, title={}, content={}",
                message.getMemberId(),
                message.getTitle(),
                message.getContent());

        // TODO: DB에 알림 저장 (나중에 알림 목록 조회용인데 있으나 없으나일듯)
        // notificationRepository.save(notification);
    }

    /**
     * 예약 확정 알림 (게스트용)
     */
    private void sendReservationConfirmedNotification(NotificationMessage message) {
        // WebSocket으로 실시간 알림 전송
        webSocketService.sendNotification(message.getMemberId(), message);

        log.info("예약 확정 알림: memberId={}, title={}, content={}",
                message.getMemberId(),
                message.getTitle(),
                message.getContent());
    }

    /**
     * 예약 취소 알림
     */
    private void sendReservationCancelledNotification(NotificationMessage message) {
        // WebSocket으로 실시간 알림 전송
        webSocketService.sendNotification(message.getMemberId(),message);

        log.info("예약 취소 알림: memberId={}, title={}, content={}",
                message.getMemberId(),
                message.getTitle(),
                message.getContent());
    }

    /**
     * 예약 완료 알림
     */
    private void sendReservationCompletedNotification(NotificationMessage message) {
        // WebSocket으로 실시간 알림 전송
        webSocketService.sendNotification(message.getMemberId(),message);

        log.info("예약 완료 알림: memberId={}, title={}, content={}",
                message.getMemberId(),
                message.getTitle(),
                message.getContent());
    }

}

package com.binbang.backend.global.service;

import com.binbang.backend.global.config.RabbitMQConfig;
import com.binbang.backend.global.dto.EmailMessage;
import com.binbang.backend.global.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * RabbitMQ 메시지 발행(Produce) 서비스
 * - 이메일 메시지 전송
 * - 알림 메시지 전송
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageProducer {

    // RabbitMQ 템플릿 갖고오기
    // RabbitMQ에 메시지를 전송하는 핵심 클래스
    private final RabbitTemplate rabbitTemplate;

    /**
     * 이메일 메시지 발행
     * @param emailMessage 이메일 정보
     *
     * 1. `emailMessage` 객체를 JSON으로 변환
     * 2. `EMAIL_EXCHANGE`로 전송
     * 3. `EMAIL_ROUTING_KEY`로 라우팅
     * 4. `email.queue`에 메시지 저장
     */
    public void sendEmailMessage(EmailMessage emailMessage){
        try{
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EMAIL_EXCHANGE,
                    RabbitMQConfig.EMAIL_ROUTING_KEY,
                    emailMessage
            );
            log.info("이메일 메시지 발행 완료: type={}, to={}",
                    emailMessage.getEmailType(),
                    emailMessage.getTo());
        } catch(Exception e){
            log.error("이메일 메시지 발행 실패: type={}, to={}, error={}",
                    emailMessage.getEmailType(),
                    emailMessage.getTo(),
                    e.getMessage(), e);
        }
    }

    /**
     * 알림 메시지 발행
     * @param notificationMessage 알림 정보
     */
    public void sendNotificationMessage(NotificationMessage notificationMessage) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,      // Exchange
                    RabbitMQConfig.NOTIFICATION_ROUTING_KEY,   // Routing Key
                    notificationMessage                         // Message
            );

            log.info("알림 메시지 발행 완료: type={}, memberId={}",
                    notificationMessage.getNotificationType(),
                    notificationMessage.getMemberId());

        } catch (Exception e) {
            log.error("알림 메시지 발행 실패: type={}, memberId={}, error={}",
                    notificationMessage.getNotificationType(),
                    notificationMessage.getMemberId(),
                    e.getMessage(), e);
        }
    }

    /**
     * 예약 확정 메시지 일괄 발송 (이메일 + 알람)
     * @param emailMessage 이메일 정보
     * @param notificationMessage 알림 정보
     */
    public void sendReservationConfirmation(
            EmailMessage emailMessage,
            NotificationMessage notificationMessage
    ){
        // 이메일 발송
        sendEmailMessage(emailMessage);
        // 앱 알림 발송
        sendNotificationMessage(notificationMessage);
    }

    /**
     * 새 예약 알림 일괄 발송 (호스트용)
     * @param emailMessage 이메일 정보
     * @param notificationMessage 알림 정보
     */
    public void sendNewReservationNotification(
            EmailMessage emailMessage,
            NotificationMessage notificationMessage) {

        // 이메일 발송
        sendEmailMessage(emailMessage);

        // 앱 알림 발송
        sendNotificationMessage(notificationMessage);

        log.info("새 예약 알림 일괄 발행 완료: reservationId={}",
                emailMessage.getReservationId());
    }

    /**
     * 예약 취소 알림 일괄 발송
     * @param emailMessage 이메일 정보
     * @param notificationMessage 알림 정보
     */
    public void sendCancellationNotification(
            EmailMessage emailMessage,
            NotificationMessage notificationMessage) {

        // 이메일 발송
        sendEmailMessage(emailMessage);

        // 앱 알림 발송
        sendNotificationMessage(notificationMessage);

        log.info("예약 취소 알림 일괄 발행 완료: reservationId={}",
                emailMessage.getReservationId());
    }

}

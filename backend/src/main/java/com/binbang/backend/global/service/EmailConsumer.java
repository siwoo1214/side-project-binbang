package com.binbang.backend.global.service;

import com.binbang.backend.global.config.RabbitMQConfig;
import com.binbang.backend.global.dto.EmailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * 이메일 메시지 소비(Consume) 서비스
 * - email.queue에서 메시지를 받아서 처리
 * - 실제 이메일 발송 수행
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailConsumer {

    private final EmailService emailService;

    /**
     * 이메일 Queue 리스너
     * - email.queue에서 메시지를 받아서 처리
     * - 자동으로 JSON을 EmailMessage 객체로 변환
     *
     * @param emailMessage 이메일 정보
     */
    @RabbitListener(queues= RabbitMQConfig.EMAIL_QUEUE)
    public void consumeEmailMessage(EmailMessage emailMessage){
        try{
            log.info("이메일 메시지 수신: type={}, to={}",
                    emailMessage.getEmailType(),
                    emailMessage.getTo());

            // 이메일 타입에 따라 분기 처리
            switch(emailMessage.getEmailType()){
                case RESERVATION_CONFIRMATION:
                    // 게스트 예약 확인 이메일
                    emailService.sendReservationConfirmationEmail(emailMessage);
                    break;

                case NEW_RESERVATION_NOTIFICATION:
                    // 호스트 새 예약 알림 이메일
                    emailService.sendNewReservationNotificationEmail(emailMessage);
                    break;

                case CANCELLATION_NOTIFICATION:
                    // 예약 취소 알림 이메리
                    emailService.sendCancellationNotificationEmail(emailMessage);
                    break;

                default:
                    log.warn("알 수 없는 이메일 타입: {}",emailMessage.getEmailType());
            }

            log.info("이메일 발송 완료: type={}, to={}",
                    emailMessage.getEmailType(),
                    emailMessage.getTo());

        } catch (Exception e){
            log.error("이메일 발송 실패: type={}, to={}, error={}",
                    emailMessage.getEmailType(),
                    emailMessage.getTo(),
                    e.getMessage(), e);

            // 예외를 다시 던지면 재시도 메커니즘 작동
            throw new RuntimeException("이메일 발송 실패. 재시도합니다.",e);
        }
    }

}

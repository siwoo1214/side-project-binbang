package com.binbang.backend.global.service;

import com.binbang.backend.global.dto.EmailMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 이메일 발송 서비스
 * - EmailConsumer에서 호출됨
 * - 실제 이메일 발송 수행
 *
 * 주요 변경 사항
 * 1. @Async 제거 - Consumer가 이미 별도 스레드에서 실행
 * 2. 파라미터 변경: Reservation → EmailMessage - 메시지 큐를 통해 필요한 데이터만 전달
 * 3. 예외 처리 변경 - 예외를 다시 던져야 RabbitMQ 재시도 메커니즘 작동
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * 게스트 예약 확인 이메일 발송
     *
     * @param emailMessage 이메일 정보
     */
    public void sendReservationConfirmationEmail(EmailMessage emailMessage) {
        try {
            String content = buildReservationConfirmationEmailContent(emailMessage);
            sendEmail(emailMessage.getTo(), emailMessage.getSubject(), content);

            log.info("예약 확인 이메일 발송 완료: to={}, reservationId={}",
                    emailMessage.getTo(),
                    emailMessage.getReservationId());

        } catch (Exception e) {
            log.error("예약 확인 이메일 발송 실패: to={}, error={}",
                    emailMessage.getTo(),
                    e.getMessage(), e);
            throw new RuntimeException("예약 확인 이메일 발송 실패", e);
        }
    }

    /**
     * 호스트 새 예약 알림 이메일 발송
     *
     * @param emailMessage 이메일 정보
     */
    public void sendNewReservationNotificationEmail(EmailMessage emailMessage) {
        try {
            String content = buildNewReservationEmailContent(emailMessage);
            sendEmail(emailMessage.getTo(), emailMessage.getSubject(), content);

            log.info("새 예약 알림 이메일 발송 완료: to={}, reservationId={}",
                    emailMessage.getTo(),
                    emailMessage.getReservationId());

        } catch (Exception e) {
            log.error("새 예약 알림 이메일 발송 실패: to={}, error={}",
                    emailMessage.getTo(),
                    e.getMessage(), e);
            throw new RuntimeException("새 예약 알림 이메일 발송 실패", e);
        }
    }

    /**
     * 예약 취소 알림 이메일 발송
     *
     * @param emailMessage 이메일 정보
     */
    public void sendCancellationNotificationEmail(EmailMessage emailMessage) {
        try {
            String content = buildCancellationEmailContent(emailMessage);
            sendEmail(emailMessage.getTo(), emailMessage.getSubject(), content);

            log.info("예약 취소 이메일 발송 완료: to={}, reservationId={}",
                    emailMessage.getTo(),
                    emailMessage.getReservationId());

        } catch (Exception e) {
            log.error("예약 취소 이메일 발송 실패: to={}, error={}",
                    emailMessage.getTo(),
                    e.getMessage(), e);
            throw new RuntimeException("예약 취소 이메일 발송 실패", e);
        }
    }

    /**
     * 실제 이메일 발송 메서드
     */
    private void sendEmail(String to, String subject, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);
        helper.setFrom("info@binbang.com");

        mailSender.send(message);
    }

    /**
     * 게스트용 예약 확인 이메일 본문 생성
     */
    private String buildReservationConfirmationEmailContent(EmailMessage emailMessage) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: 'Malgun Gothic', sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #2196F3; color: white; padding: 20px; text-align: center; border-radius: 5px; }
                        .content { background-color: #f9f9f9; padding: 20px; margin-top: 20px; border-radius: 5px; }
                        .info-row { margin: 10px 0; padding: 10px; background-color: white; border-radius: 3px; }
                        .label { font-weight: bold; color: #555; }
                        .value { color: #333; margin-left: 10px; }
                        .footer { margin-top: 20px; text-align: center; color: #777; font-size: 12px; }
                        .highlight { color: #2196F3; font-weight: bold; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>✅ 예약이 완료되었습니다!</h2>
                        </div>
                        
                        <div class="content">
                            <p>안녕하세요, <strong>%s</strong>님!</p>
                            <p>예약이 성공적으로 완료되었습니다.</p>
                            
                            <h3>🏠 숙소 정보</h3>
                            
                            <div class="info-row">
                                <span class="label">숙소명:</span>
                                <span class="value">%s</span>
                            </div>
                            
                            <div class="info-row">
                                <span class="label">호스트:</span>
                                <span class="value">%s</span>
                            </div>
                            
                            <div class="info-row">
                                <span class="label">체크인:</span>
                                <span class="value">%s</span>
                            </div>
                            
                            <div class="info-row">
                                <span class="label">체크아웃:</span>
                                <span class="value">%s</span>
                            </div>
                            
                            <div class="info-row">
                                <span class="label">투숙 인원:</span>
                                <span class="value">%d명</span>
                            </div>
                            
                            <div class="info-row">
                                <span class="label">총 금액:</span>
                                <span class="value highlight">%,d원</span>
                            </div>
                        </div>
                        
                        <div class="footer">
                            <p>즐거운 여행 되세요!</p>
                            <p><strong>빈방</strong> 팀 드림</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                emailMessage.getGuestName(),
                emailMessage.getAccommodationName(),
                emailMessage.getHostName(),
                emailMessage.getCheckInDate(),
                emailMessage.getCheckOutDate(),
                emailMessage.getGuestCount(),
                emailMessage.getTotalPrice()
        );
    }

    /**
     * 호스트용 새 예약 알림 이메일 본문 생성
     */
    private String buildNewReservationEmailContent(EmailMessage emailMessage) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: 'Malgun Gothic', sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 5px; }
                        .content { background-color: #f9f9f9; padding: 20px; margin-top: 20px; border-radius: 5px; }
                        .info-row { margin: 10px 0; padding: 10px; background-color: white; border-radius: 3px; }
                        .label { font-weight: bold; color: #555; }
                        .value { color: #333; margin-left: 10px; }
                        .footer { margin-top: 20px; text-align: center; color: #777; font-size: 12px; }
                        .highlight { color: #4CAF50; font-weight: bold; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>🎉 새로운 예약이 접수되었습니다!</h2>
                        </div>
                        
                        <div class="content">
                            <p>안녕하세요, <strong>%s</strong>님!</p>
                            <p>'<strong>%s</strong>'에 새로운 예약이 접수되었습니다.</p>
                            
                            <h3>📋 예약 정보</h3>
                            
                            <div class="info-row">
                                <span class="label">예약자:</span>
                                <span class="value">%s</span>
                            </div>
                            
                            <div class="info-row">
                                <span class="label">체크인:</span>
                                <span class="value">%s</span>
                            </div>
                            
                            <div class="info-row">
                                <span class="label">체크아웃:</span>
                                <span class="value">%s</span>
                            </div>
                            
                            <div class="info-row">
                                <span class="label">투숙 인원:</span>
                                <span class="value">%d명</span>
                            </div>
                            
                            <div class="info-row">
                                <span class="label">총 금액:</span>
                                <span class="value highlight">%,d원</span>
                            </div>
                        </div>
                        
                        <div class="footer">
                            <p>감사합니다.</p>
                            <p><strong>빈방</strong> 팀 드림</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                emailMessage.getHostName(),
                emailMessage.getAccommodationName(),
                emailMessage.getGuestName(),
                emailMessage.getCheckInDate(),
                emailMessage.getCheckOutDate(),
                emailMessage.getGuestCount(),
                emailMessage.getTotalPrice()
        );
    }

    /**
     * 예약 취소 이메일 본문 생성
     */
    private String buildCancellationEmailContent(EmailMessage emailMessage) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: 'Malgun Gothic', sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #F44336; color: white; padding: 20px; text-align: center; border-radius: 5px; }
                        .content { background-color: #f9f9f9; padding: 20px; margin-top: 20px; border-radius: 5px; }
                        .info-row { margin: 10px 0; padding: 10px; background-color: white; border-radius: 3px; }
                        .label { font-weight: bold; color: #555; }
                        .value { color: #333; margin-left: 10px; }
                        .footer { margin-top: 20px; text-align: center; color: #777; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>❌ 예약이 취소되었습니다</h2>
                        </div>
                        
                        <div class="content">
                            <p>안녕하세요!</p>
                            <p>'<strong>%s</strong>'의 예약이 취소되었습니다.</p>
                            
                            <div class="info-row">
                                <span class="label">예약 기간:</span>
                                <span class="value">%s ~ %s</span>
                            </div>
                            
                            <div class="info-row">
                                <span class="label">예약자:</span>
                                <span class="value">%s</span>
                            </div>
                        </div>
                        
                        <div class="footer">
                            <p><strong>빈방</strong> 팀 드림</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                emailMessage.getAccommodationName(),
                emailMessage.getCheckInDate(),
                emailMessage.getCheckOutDate(),
                emailMessage.getGuestName()
        );
    }
}
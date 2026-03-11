package com.binbang.backend.global.service;

import com.binbang.backend.chat.dto.response.ChatMessageResponse;
import com.binbang.backend.global.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * WebSocket 메시지 전송 서비스
 *
 * 역할:
 * - 특정 회원에게 실시간 알림 전송
 * - WebSocket을 통한 메시지 브로드캐스트
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketService {

    /**
     * SimpMessagingTemplate
     * -> WebSocket으로 메시지를 보내는 핵심 클래스
     *
     * Spring이 자동으로 생성해서 주입해줌
     */
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 특정 회원에게 알림 전송
     *
     * @param memberId 수신자 ID
     * @param notification 알림 내용
     *
     * 동작:
     * 1. NotificationMessage 객체를 JSON으로 변환
     * 2. /topic/notifications/{memberId} 채널로 전송
     * 3. 해당 채널을 구독 중인 클라이언트가 수신
     */
    public void sendNotification(Long memberId, NotificationMessage notification){
        try{
            // 메시지 전송
            // destination : /topic/notifications/1 (memberId가 1일 때)
            // payload : NotificationMessage 객체 (자동으로 직렬화)
            messagingTemplate.convertAndSend(
                    "/topic/notifications/"+memberId,  // 전송경로
                    notification  //전송 내용
            );

            log.info("WebSocket 알림 전송 완료: memberId={}, type={}",
                    memberId,
                    notification.getNotificationType());
        }catch(Exception e){
            log.error("WebSocket 알림 전송 실패 error={}",e.getMessage());
            // 예외를 던지지 않는 이유는 WebSocket 전송 실패해도 이메일은 정상적으로 가야함
        }
    }

    /**
     * 특정 회원에게 채팅 메시지 전송
     *
     * @param memberId 수신자 회원 ID
     * @param message 채팅 메시지
     */
    public void sendChatMessage(Long memberId, ChatMessageResponse message) {
        try {
            // /queue/chat/{memberId} 채널로 메시지 전송
            // /queue: 특정 1명에게만 전달하는 1:1 경로
            messagingTemplate.convertAndSend(
                    "/queue/chat/" + memberId,
                    message
            );

            log.info("WebSocket 채팅 메시지 전송 완료: receiverId={}, messageId={}",
                    memberId,
                    message.getMessageId());

        } catch (Exception e) {
            log.error("WebSocket 채팅 메시지 전송 실패: receiverId={}, error={}",
                    memberId,
                    e.getMessage(), e);
        }
    }

    /**
     * 여러 회원에게 동시 알림 전송 (Broadcast)
     *
     * 사용 예:
     * - 전체 공지사항
     * - 시스템 점검 알림
     *
     * 현재는 구현만 해두고 나중에 사용
     */
    public void broadcastNotification(NotificationMessage notification){
        try{
          messagingTemplate.convertAndSend(
                  "/topic/notifications/all",
                  notification
          );
            log.info("WebSocket 전체 알림 전송 완료: type={}",
                    notification.getNotificationType());
        }catch (Exception e){
            log.error("WebSocket 전체 알림 전송 실패: error={}",
                    e.getMessage(), e);
        }
    }

}

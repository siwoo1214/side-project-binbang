package com.binbang.backend.chat.controller;

import com.binbang.backend.chat.dto.request.ChatMessageRequest;
import com.binbang.backend.chat.dto.response.ChatMessageResponse;
import com.binbang.backend.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

/**
 * 채팅 WebSocket 컨트롤러
 *
 * 엔드포인트:
 * - /app/chat/send : 메시지 전송 (WebSocket)
 *
 * 흐름:
 * 1. 클라이언트가 /app/chat/send로 메시지 전송
 * 2. 이 컨트롤러가 받아서 ChatService 호출
 * 3. ChatService가 DB 저장 + WebSocket으로 상대방에게 전송
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;

    /**
     * WebSocket으로 메시지 전송
     *
     * 클라이언트 → /app/chat/send
     *
     * @param request 메시지 내용
     * @param headerAccessor WebSocket 세션 정보
     */
    @MessageMapping("/chat/send")
    public void sendMessage(
            @Payload ChatMessageRequest request,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        // WebSocket 세션에서 memberId 추출
        Long senderId = request.getSenderId();

        if(senderId==null){
            log.error("WebSocket 메시지에 senderId가 없습니다 : {}",request);
        }

        log.info("WebSocket 메시지 수신: chatRoomId={}, content={}",
                request.getChatRoomId(), request.getContent());

        // 메시지 전송 (DB 저장 + WebSocket 전송)
        ChatMessageResponse response = chatService.sendMessage(
                request.getChatRoomId(),
                senderId,
                request
        );

        log.info("WebSocket 메시지 처리 완료: messageId={}", response.getMessageId());
    }
}
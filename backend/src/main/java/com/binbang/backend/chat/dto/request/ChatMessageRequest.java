package com.binbang.backend.chat.dto.request;

import com.binbang.backend.chat.entity.ChatMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 채팅 메시지 전송 요청 DTO
 *
 * 사용:
 * - REST API: POST /api/chat/rooms/{chatRoomId}/messages
 * - WebSocket: /app/chat/send
 */

@Data
@NoArgsConstructor
public class ChatMessageRequest {

    // 채팅방 ID
    private Long chatRoomId;

    // 보내는 사람
    private Long senderId;

    // 메시지
    @NotBlank(message = "메시지 내용은 필수입니다")
    private String content;

    // 메시지 타ㅣㅂ (기본값 : TEXT)
    private ChatMessage.MessageType messageType = ChatMessage.MessageType.TEXT;

}

package com.binbang.backend.chat.dto.response;

import com.binbang.backend.chat.entity.ChatMessage;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 채팅 메시지 응답 DTO
 *
 * 사용:
 * - 메시지 목록 조회
 * - WebSocket 메시지 전송
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {

    private Long messageId;

    private Long chatRoomId;

    private Long senderId;

    private String senderName;

    private ChatMessage.MessageType messageType;

    private String content;

    private String imageUrl;

    private Boolean isRead;

    private LocalDateTime createdAt;

    /**
     * 엔티티 → DTO 변환
     */
    public static ChatMessageResponse from(ChatMessage message) {
        return ChatMessageResponse.builder()
                .messageId(message.getMessageId())
                .chatRoomId(message.getChatRoom().getChatRoomId())
                .senderId(message.getSender() != null ? message.getSender().getMemberId() : null)
                .senderName(message.getSender() != null ? message.getSender().getName() : "시스템")
                .messageType(message.getMessageType())
                .content(message.getContent())
                .imageUrl(message.getImageUrl())
                .isRead(message.getIsRead())
                .createdAt(message.getCreatedAt())
                .build();
    }

}

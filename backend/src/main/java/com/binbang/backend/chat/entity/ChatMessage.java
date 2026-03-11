package com.binbang.backend.chat.entity;

import com.binbang.backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.awt.*;
import java.time.LocalDateTime;

/**
 * 채팅 메시지 엔티티
 *
 * 역할:
 * - 채팅방 내의 실제 메시지
 * - 텍스트, 이미지, 시스템 메시지 지원
 *
 * 관계:
 * - ChatRoom (N:1): 어떤 채팅방의 메시지인지
 * - Member (N:1): 누가 보냈는지
 */
@Entity
@Data
@NoArgsConstructor
@Table(name = "chat_message",
        indexes = {
            @Index(name = "idx_chat_room_created", columnList = "chat_room_id,created_at"),
            @Index(name = "idx_sender_created", columnList = "sender_id,created_at")
        })
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    /**
     * 채팅방
     * - 어떤 채팅방의 메시지인지
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    // 발신자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Member sender;

    /**
     * 메시지 타입
     * - TEXT : 일반 텍스트
     * - IMAGE : 이미지
     * - SYSTEM : 시스템 메시지 (ex: 예약이 확정되었습니다.)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageType messageType;

    // 메시지 내용 - TEXT 타입
    @Column(columnDefinition = "TEXT")
    private String content;

    // 이미지 url - IMAGE 타입
    @Column(length = 500)
    private String imageUrl;

    // 읽씹 방지 읽음 여부
    @Column(nullable = false)
    private Boolean isRead = false;

    // 메시지 생성 시간
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 메시지 타입
    public enum MessageType {
        TEXT,      // 일반 텍스트 메시지
        IMAGE,     // 이미지 메시지
        SYSTEM     // 시스템 메시지
    }

    // === 편의 메서드 ===

    /**
     * 텍스트 메시지 생성
     */
    public static ChatMessage createTextMessage(
            ChatRoom chatRoom,
            Member sender,
            String content
    ) {
        ChatMessage message = new ChatMessage();
        message.setChatRoom(chatRoom);
        message.setSender(sender);
        message.setMessageType(MessageType.TEXT);
        message.setContent(content);
        return message;
    }

    /**
     * 이미지 메시지 생성
     */
    public static ChatMessage createImageMessage(
            ChatRoom chatRoom,
            Member sender,
            String imageUrl
    ) {
        ChatMessage message = new ChatMessage();
        message.setChatRoom(chatRoom);
        message.setSender(sender);
        message.setMessageType(MessageType.IMAGE);
        message.setImageUrl(imageUrl);
        return message;
    }

    /**
     * 시스템 메시지 생성
     */
    public static ChatMessage createSystemMessage(
            ChatRoom chatRoom,
            String content
    ) {
        ChatMessage message = new ChatMessage();
        message.setChatRoom(chatRoom);
        message.setSender(null);  // 시스템 메시지는 발신자 없음
        message.setMessageType(MessageType.SYSTEM);
        message.setContent(content);
        message.setIsRead(true);  // 시스템 메시지는 자동 읽음 처리
        return message;
    }

}

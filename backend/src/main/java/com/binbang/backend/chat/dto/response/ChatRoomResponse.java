package com.binbang.backend.chat.dto.response;

import com.binbang.backend.chat.entity.ChatMessage;
import com.binbang.backend.chat.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 채팅방 응답 DTO
 *
 * 사용:
 * - 채팅방 목록 조회 (Phase 4)
 * - 채팅방 생성/조회
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponse {

    private Long chatRoomId;

    private Long reservationId;

    private String accommodationName;

    private Long hostId;

    private String hostName;

    private Long guestId;

    private String guestName;

    private String lastMessage;

    private LocalDateTime lastMessageTime;

    private Integer unreadCount;

    private LocalDateTime createdAt;

    /**
     * 엔티티 → DTO 변환 (기본)
     */
    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return ChatRoomResponse.builder()
                .chatRoomId(chatRoom.getChatRoomId())
                .reservationId(chatRoom.getReservation().getReservationId())
                .accommodationName(chatRoom.getReservation().getAccommodation().getName())
                .hostId(chatRoom.getHost().getMemberId())
                .hostName(chatRoom.getHost().getName())
                .guestId(chatRoom.getGuest().getMemberId())
                .guestName(chatRoom.getGuest().getName())
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }

    /**
     * 엔티티 → DTO 변환 (마지막 메시지 포함)
     */
    public static ChatRoomResponse from(
            ChatRoom chatRoom,
            ChatMessage lastMessage,
            Integer unreadCount
    ) {
        ChatRoomResponseBuilder builder = ChatRoomResponse.builder()
                .chatRoomId(chatRoom.getChatRoomId())
                .reservationId(chatRoom.getReservation().getReservationId())
                .accommodationName(chatRoom.getReservation().getAccommodation().getName())
                .hostId(chatRoom.getHost().getMemberId())
                .hostName(chatRoom.getHost().getName())
                .guestId(chatRoom.getGuest().getMemberId())
                .guestName(chatRoom.getGuest().getName())
                .unreadCount(unreadCount)
                .createdAt(chatRoom.getCreatedAt());

        // 마지막 메시지가 있으면 추가
        if (lastMessage != null) {
            builder.lastMessage(lastMessage.getContent())
                    .lastMessageTime(lastMessage.getCreatedAt());
        }

        return builder.build();
    }

}

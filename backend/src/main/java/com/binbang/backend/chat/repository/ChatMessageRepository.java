package com.binbang.backend.chat.repository;

import com.binbang.backend.chat.entity.ChatMessage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 채팅방의 메시지 목록 조회 (최신순)
     * - 채팅방 화면 메시지 보여주기 위헤
     */
    List<ChatMessage> findByChatRoom_ChatRoomIdOrderByCreatedAtDesc(Long chatRoomId);

    /**
     * 채팅방의 메시지 목록 조회 (페이징)
     * - 무한 스크롤 만들기 위해사ㅓ
     */
    Page<ChatMessage> findByChatRoom_ChatRoomIdOrderByCreatedAtDesc(
            Long chatRoomId,
            Pageable pageable
    );

    /**
     * 안 읽은 메시지 개수 조회
     * - 채팅방 목록에 빨갠 뱃지 표시용
     *
     * @param chatRoomId 채팅방 ID
     * @param memberId 내 회원 ID
     */
    @Query("SELECT COUNT(cm) FROM ChatMessage cm " +
            "WHERE cm.chatRoom.chatRoomId = :chatRoomId " +
            "AND cm.sender.memberId != :memberId " +
            "AND cm.isRead = false")
    Integer countUnreadMessages(
            @Param("chatRoomId") Long chatRoomId,
            @Param("memberId") Long memberId
    );

    /**
     * 마지막 메시지 조회
     * - 채팅방 목록에서 미리보기용
     */
    Optional<ChatMessage> findTopByChatRoom_ChatRoomIdOrderByCreatedAtDesc(
            Long chatRoomId
    );

    /**
     * 안 읽은 메시지 목록 조회
     * - 읽음 처리할 때 사용
     */
    @Query("SELECT cm FROM ChatMessage cm " +
            "WHERE cm.chatRoom.chatRoomId = :chatRoomId " +
            "AND cm.sender.memberId != :memberId " +
            "AND cm.isRead = false")
    List<ChatMessage> findUnreadMessages(
            @Param("chatRoomId") Long chatRoomId,
            @Param("memberId") Long memberId
    );

}

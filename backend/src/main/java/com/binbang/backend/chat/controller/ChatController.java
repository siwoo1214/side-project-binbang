package com.binbang.backend.chat.controller;

import com.binbang.backend.chat.dto.request.ChatMessageRequest;
import com.binbang.backend.chat.dto.response.ChatMessageResponse;
import com.binbang.backend.chat.dto.response.ChatRoomResponse;
import com.binbang.backend.chat.service.ChatService;
import com.binbang.backend.member.repository.MemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final MemberRepository memberRepository;

    /**
     * 내 채팅방 목록 조회
     * GET /api/chat/rooms
     */
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponse>> getMyChatRooms(
            @AuthenticationPrincipal String email
    ) {
        log.info("채팅방 목록 조회: email={}", email);
        return ResponseEntity.ok(chatService.getMyChatRooms(email));
    }

    /**
     * 채팅방 생성 또는 조회 (예약 기반)
     * GET /api/chat/reservations/{reservationId}/room
     */
    @GetMapping("/reservations/{reservationId}/room")
    public ResponseEntity<ChatRoomResponse> getOrCreateChatRoom(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal String email
    ) {
        log.info("채팅방 조회/생성: reservationId={}, email={}", reservationId, email);
        Long memberId = memberRepository.findByEmail(email)
                .orElseThrow().getMemberId();
        return ResponseEntity.ok(chatService.getOrCreateChatRoom(reservationId, memberId));
    }

    /**
     * 채팅방 조회
     * GET /api/chat/rooms/{chatRoomId}
     */
    @GetMapping("/rooms/{chatRoomId}")
    public ResponseEntity<ChatRoomResponse> getChatRoom(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal String email
    ) {
        Long memberId = memberRepository.findByEmail(email)
                .orElseThrow().getMemberId();
        return ResponseEntity.ok(chatService.getChatRoom(chatRoomId, memberId));
    }

    /**
     * 메시지 목록 조회
     * GET /api/chat/rooms/{chatRoomId}/messages
     */
    @GetMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getChatMessages(
            @PathVariable Long chatRoomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal String email
    ) {
        Long memberId = memberRepository.findByEmail(email)
                .orElseThrow().getMemberId();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(chatService.getChatMessages(chatRoomId, memberId, pageable));
    }

    /**
     * 메시지 전송 (REST)
     * POST /api/chat/rooms/{chatRoomId}/messages
     */
    @PostMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @PathVariable Long chatRoomId,
            @Valid @RequestBody ChatMessageRequest request,
            @AuthenticationPrincipal String email
    ) {
        Long memberId = memberRepository.findByEmail(email)
                .orElseThrow().getMemberId();
        request.setChatRoomId(chatRoomId);
        request.setSenderId(memberId);
        return ResponseEntity.ok(chatService.sendMessage(chatRoomId, memberId, request));
    }
}

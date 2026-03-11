package com.binbang.backend.chat.service;

import com.binbang.backend.chat.dto.request.ChatMessageRequest;
import com.binbang.backend.chat.dto.response.ChatMessageResponse;
import com.binbang.backend.chat.dto.response.ChatRoomResponse;
import com.binbang.backend.chat.entity.ChatMessage;
import com.binbang.backend.chat.entity.ChatRoom;
import com.binbang.backend.chat.exception.ChatRoomNotFoundException;
import com.binbang.backend.chat.repository.ChatMessageRepository;
import com.binbang.backend.chat.repository.ChatRoomRepository;
import com.binbang.backend.global.exception.CustomException;
import com.binbang.backend.global.service.WebSocketService;
import com.binbang.backend.member.entity.Member;
import com.binbang.backend.member.repository.MemberRepository;
import com.binbang.backend.reservation.entity.Reservation;
import com.binbang.backend.reservation.exception.ReservationNotFoundException;
import com.binbang.backend.reservation.repository.ReservationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 채팅 서비스
 *
 * 역할:
 * - 채팅방 생성/조회
 * - 메시지 전송/조회
 * - 읽음 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final WebSocketService webSocketService;

    /**
     * 채팅방 생성 또는 조회
     *
     * 흐름:
     * 1. 예약 조회
     * 2. 권한 체크 (호스트 or 게스트만 접근 가능)
     * 3. 기존 채팅방 조회 또는 신규 생성
     *
     * @param reservationId 예약 ID
     * @param memberId 요청자 회원 ID
     * @return 채팅방 정보
     */
    @Transactional
    public ChatRoomResponse getOrCreateChatRoom(Long reservationId, Long memberId){

        // 1. 예약 조회
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        // 2. 권한 체크
        Long hostId = reservation.getAccommodation().getMember().getMemberId();
        Long guestId = reservation.getMember().getMemberId();

        if(!memberId.equals(hostId) && !memberId.equals(guestId)){
            throw new CustomException(HttpStatus.FORBIDDEN,
                    "해당 예약의 채팅방에 접근할 권한이 없습니다.");
        }

        // 3. 기존 채팅방 조회 또는 생성
        ChatRoom chatRoom = chatRoomRepository.findByReservation_ReservationId(reservationId)
                .orElseGet(() ->{
                    ChatRoom newRoom = new ChatRoom();
                    newRoom.setReservation(reservation);
                    newRoom.setHost(reservation.getAccommodation().getMember());
                    newRoom.setGuest(reservation.getMember());

                    return chatRoomRepository.save(newRoom);

                });

        return ChatRoomResponse.from(chatRoom);
    }

    /**
     * 메시지 전송
     *
     * 흐름:
     * 1. 채팅방 조회
     * 2. 권한 체크
     * 3. 메시지 저장
     * 4. 채팅방 업데이트 시간 갱신
     * 5. WebSocket으로 상대방에게 실시간 전송
     *
     * @param chatRoomId 채팅방 ID
     * @param senderId 발신자 ID
     * @param request 메시지 내용
     * @return 저장된 메시지 정보
     */
    @Transactional
    public ChatMessageResponse sendMessage(
            Long chatRoomId,
            Long senderId,
            ChatMessageRequest request
    ) {
        // 1. 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(()-> new ChatRoomNotFoundException(chatRoomId));

        // 2. 권한 체크 (채팅방 참여자만 메시지 전송 가능)
        if(!chatRoom.isMember(senderId)){
            throw new CustomException(HttpStatus.FORBIDDEN,
                    "해당 채팅방에 메시지를 보낼 권한이 없음");
        }

        // 3. 발신자 조회
        Member sender = chatRoom.getHost().getMemberId().equals(senderId) ? chatRoom.getHost() : chatRoom.getGuest();

        // 4. 메시지 생성 및 저장
        ChatMessage message = ChatMessage.createTextMessage(
                chatRoom,sender, request.getContent()
        );

        ChatMessage savedMessage = chatMessageRepository.save(message);

        // 5. 채팅방 업데이트 시간 갱신
        chatRoom.setUpdatedAt(LocalDateTime.now());
        chatRoomRepository.save(chatRoom);

        // 6. 응답 DTO 생성
        ChatMessageResponse response = ChatMessageResponse.from(savedMessage);

        // 7. websocket으로 상대방에게 실시간 전송
        Member reciever = chatRoom.getOtherMember(senderId);
        if(reciever != null){
            webSocketService.sendChatMessage(reciever.getMemberId(), response);
        }

        log.info("메시지 전송 완료: chatRoomId={}, senderId={}, messageId={}",
                chatRoomId, senderId, savedMessage.getMessageId());

        return response;
    }

    /**
     * 채팅 메시지 목록 조회
     *
     * @param chatRoomId 채팅방 ID
     * @param memberId 요청자 ID
     * @param pageable 페이징 정보
     * @return 메시지 목록
     */
    @Transactional
    public List<ChatMessageResponse> getChatMessages(
            Long chatRoomId,
            Long memberId,
            Pageable pageable
    ) {

        // 1. 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatRoomNotFoundException(chatRoomId));

        // 2. 권한 체크
        if (!chatRoom.isMember(memberId)) {
            throw new CustomException(HttpStatus.FORBIDDEN,
                    "해당 채팅방에 접근할 권한이 없습니다");
        }

        // 3. 메시지 조회
        Page<ChatMessage> messages = chatMessageRepository
                .findByChatRoom_ChatRoomIdOrderByCreatedAtDesc(chatRoomId, pageable);

        // 4. DTO 변환
        return messages.getContent().stream()
                .map(ChatMessageResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 내 채팅방 목록 조회
     *
     * @param email 로그인한 사용자 이메일
     * @return 채팅방 목록
     */
    @Transactional
    public List<ChatRoomResponse> getMyChatRooms(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));

        List<ChatRoom> rooms = chatRoomRepository.findMyRooms(member.getMemberId());

        return rooms.stream()
                .map(room -> {
                    ChatMessage lastMsg = chatMessageRepository
                            .findTopByChatRoom_ChatRoomIdOrderByCreatedAtDesc(room.getChatRoomId())
                            .orElse(null);
                    return ChatRoomResponse.from(room, lastMsg, 0);
                })
                .collect(Collectors.toList());
    }

    /**
     * 예약 생성 시 채팅방 자동 생성 (ReservationService에서 호출)
     * - 이미 채팅방이 있으면 그냥 반환 (중복 생성 방지)
     *
     * @param reservation 생성된 예약 엔티티
     */
    @Transactional
    public void createChatRoomForReservation(Reservation reservation) {
        // 이미 채팅방이 있으면 스킵 (멱등성 보장)
        boolean exists = chatRoomRepository
                .existsByReservation_ReservationId(reservation.getReservationId());
        if (exists) {
            log.info("채팅방 이미 존재: reservationId={}", reservation.getReservationId());
            return;
        }

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setReservation(reservation);
        chatRoom.setHost(reservation.getAccommodation().getMember());
        chatRoom.setGuest(reservation.getMember());
        chatRoomRepository.save(chatRoom);

        log.info("채팅방 생성 완료: reservationId={}, host={}, guest={}",
                reservation.getReservationId(),
                reservation.getAccommodation().getMember().getEmail(),
                reservation.getMember().getEmail());
    }

    /**
     * 채팅방 ID로 채팅방 조회
     *
     * @param chatRoomId 채팅방 ID
     * @param memberId 요청자 ID
     * @return 채팅방 정보
     */
    @Transactional
    public ChatRoomResponse getChatRoom(Long chatRoomId, Long memberId) {

        // 1. 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatRoomNotFoundException(chatRoomId));

        // 2. 권한 체크
        if (!chatRoom.isMember(memberId)) {
            throw new CustomException(HttpStatus.FORBIDDEN,
                    "해당 채팅방에 접근할 권한이 없습니다");
        }

        return ChatRoomResponse.from(chatRoom);
    }

}

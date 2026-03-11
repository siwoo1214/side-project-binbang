package com.binbang.backend.chat.entity;

import com.binbang.backend.member.entity.Member;
import com.binbang.backend.reservation.entity.Reservation;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 채팅방 엔티티
 *
 * 역할:
 * - 호스트와 게스트 간의 1:1 채팅방
 * - 예약당 하나의 채팅방 생성
 *
 * 관계:
 * - Reservation (1:1): 어떤 예약에 대한 채팅인지
 * - Member (N:1): 호스트, 게스트
 */
@Entity
@Data
@NoArgsConstructor
@Table(name="chat_room")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatRoomId;

    /**
     * 예약 정보
     * - 채팅의 컨텍스트
     * - 예약 1개당 채팅방 1개
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="reservation_id", unique = true, nullable = false)
    private Reservation reservation;

    // 호스트
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_member_id", nullable = false)
    Member host;

    // 게스트
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_member_id", nullable = false)
    Member guest;

    // 채팅방 생성 시간
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 채팅방 업데이트 시간
     * - 새 메시지가 올 때마다 업데이트
     * - 채팅방 목록 정렬에 사용
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ========= 유틸리티 메소드 =============

    // 게스트, 호스트가 이 채팅방의 참여자인지 확인
    public boolean isMember(Long memberId){
        return host.getMemberId().equals(memberId) || guest.getMemberId().equals(memberId);
    }

    // 채팅방 상대방 회원정보 가져오기
    public Member getOtherMember(Long myMemberId){
        if(host.getMemberId().equals(myMemberId)){
            return guest;
        } else if(guest.getMemberId().equals(myMemberId)){
            return host;
        }
        return null;
    }


}

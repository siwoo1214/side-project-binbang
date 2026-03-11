package com.binbang.backend.chat.repository;

import com.binbang.backend.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 예약 ID로 채팅방 조회
    Optional<ChatRoom> findByReservation_ReservationId(Long reservationId);

    /**
     * 내가 참여한 채팅방 목록 조회 (호스트 or 개스트)
     * - 최신 대회 순으로 정렬
     */
    @Query("SELECT cr FROM ChatRoom cr " +
            "WHERE cr.host.memberId = :memberId " +
            "OR cr.guest.memberId = :memberId " +
            "ORDER BY cr.updatedAt DESC")
    List<ChatRoom> findMyRooms(@Param("memberId") Long memberId);

    /**
     * 채팅방 존재 여부 확인
     * - 중복 생성 방지용
     */
    boolean existsByReservation_ReservationId(Long reservationId);

}

package com.binbang.backend.reservation.repository;

import com.binbang.backend.accommodation.entity.AccommodationStatus;
import com.binbang.backend.reservation.entity.Reservation;
import com.binbang.backend.reservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 예약 Repository
 * - 예약 CRUD
 * - 회원별/숙소별 예약 조회
 * - 날짜 중복 체크
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * 특정 회원의 모든 예약 조회
     *
     * @param memberId 회원 ID
     * @return 예약 목록 (최신순)
     */
    List<Reservation> findByMember_MemberIdOrderByReservedAtDesc(Long memberId);

    /**
     * 특정 숙소의 모든 예약 조회 (호스트용)
     *
     * @param accommodationId 숙소 ID
     * @return 예약 목록 (최신순)
     */
    List<Reservation> findByAccommodation_AccommodationIdOrderByReservedAtDesc(Long accommodationId);

    /**
     * 예약 ID와 회원 ID로 예약 조회 (권한 체크용)
     * - 자신의 예약만 조회/취소할 수 있도록
     *
     * @param reservationId 예약 ID
     * @param memberId 회원 ID
     * @return 예약 정보
     */
    Optional<Reservation> findByReservationIdAndMember_MemberId(Long reservationId, Long memberId);

    /**
     * 특정 숙소의 특정 날짜 범위에 중복 예약이 있는지 확인
     * - 예약 가능 여부 판단용
     * - 기존 예약의 (체크인 < 새 체크아웃) AND (체크아웃 > 새 체크인) 체크
     *
     * @param accommodationId 숙소 ID
     * @param checkInDate 체크인 날짜
     * @param checkOutDate 체크아웃 날짜
     * @param status 예약 상태 (RESERVED만 체크)
     * @return 중복 예약 존재 여부
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM Reservation r " +
            "WHERE r.accommodation.accommodationId = :accommodationId " +
            "AND r.status = :status " +
            "AND r.checkInDate < :checkOutDate " +
            "AND r.checkOutDate > :checkInDate")
    boolean existsOverlappingReservation(
            @Param("accommodationId") Long accommodationId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("status") ReservationStatus status
    );

    // 특정 숙소에서 status에 따라 조회
    List<Reservation>  findByAccommodation_AccommodationIdAndStatus(Long accommodationId, ReservationStatus status);

    /**
     * 특정 회원의 예약 개수 조회
     *
     * @param memberId 회원 ID
     * @return 예약 개수
     */
    long countByMember_MemberId(Long memberId);

    /**
     * 특정 숙소의 예약 개수 조회 (호스트용)
     *
     * @param accommodationId 숙소 ID
     * @return 예약 개수
     */
    long countByAccommodation_AccommodationId(Long accommodationId);

    /**
     * 특정 회원의 특정 상태 예약 조회
     *
     * @param memberId 회원 ID
     * @param status 예약 상태
     * @return 예약 목록
     */
    List<Reservation> findByMember_MemberIdAndStatusOrderByReservedAtDesc(Long memberId, ReservationStatus status);
}

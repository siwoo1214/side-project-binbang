package com.binbang.backend.reservation.dto.response;

/**
 * 예약 목록 응답 DTO (간략 버전)
 * - 목록 조회 시 사용
 * - 핵심 정보만 포함
 */

import com.binbang.backend.reservation.entity.Reservation;
import com.binbang.backend.reservation.entity.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationListResponse {

    // ===== 예약 기본 정보 =====
    private Long reservationId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer personnel;
    private Long totalPrice;
    private ReservationStatus status;
    private LocalDateTime reservedAt;

    // ===== 숙소 정보 (간략) =====
    private Long accommodationId;
    private String accommodationName;

    // ===== 호스트 정보 (게스트가 볼 때) =====
    private String hostName;

    // ===== 게스트 정보 (호스트가 볼 때) =====
    private String guestName;

    /**
     * Reservation 엔티티로부터 간략 DTO 생성
     *
     * @param reservation 예약 엔티티
     * @return 예약 목록 응답 DTO
     * reservation 에서 데이터 받아와서 리스트로 보여줌
     */
    public static ReservationListResponse from(Reservation reservation) {
        return ReservationListResponse.builder()
                .reservationId(reservation.getReservationId())
                .checkInDate(reservation.getCheckInDate())
                .checkOutDate(reservation.getCheckOutDate())
                .personnel(reservation.getPersonnel())
                .totalPrice(reservation.getTotalPrice())
                .status(reservation.getStatus())
                .reservedAt(reservation.getReservedAt())

                .accommodationId(reservation.getAccommodation().getAccommodationId())
                .accommodationName(reservation.getAccommodation().getName())

                .hostName(reservation.getAccommodation().getMember().getName())
                .guestName(reservation.getMember().getName())

                .build();
    }
}
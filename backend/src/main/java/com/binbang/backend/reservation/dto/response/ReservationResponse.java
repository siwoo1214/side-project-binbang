package com.binbang.backend.reservation.dto.response;

import com.binbang.backend.reservation.entity.Reservation;
import com.binbang.backend.reservation.entity.ReservationStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 예약 상세 응답 DTO
 * - 예약 정보
 * - 숙소 정보
 * - 호스트 정보
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {

    // ===== 예약 정보 =====
    private Long reservationId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer personnel;
    private Long totalPrice;
    private Long nights;  // 숙박 일수
    private ReservationStatus status;
    private LocalDateTime reservedAt;

    // ===== 숙소 정보 =====
    private Long accommodationId;
    private String accommodationName;
    private String accommodationAddress;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;

    // ===== 호스트 정보 =====
    private String hostName;
    private String hostEmail;
    private String hostPhone;

    // ===== 게스트 정보 (호스트가 조회할 때 필요) =====
    private String guestName;
    private String guestEmail;
    private String guestPhone;

    /**
     * Reservation 엔티티로부터 DTO 생성
     *
     * @param reservation 예약 엔티티
     * @return 예약 응답 DTO
     */
    public static ReservationResponse from(Reservation reservation) {
        // 숙박 일수 계산
        long nights = java.time.temporal.ChronoUnit.DAYS.between(
                reservation.getCheckInDate(),
                reservation.getCheckOutDate()
        );

        return ReservationResponse.builder()
                // 예약 정보
                .reservationId(reservation.getReservationId())
                .reservedAt(LocalDateTime.now())
                .checkInDate(reservation.getCheckInDate())
                .checkOutDate(reservation.getCheckOutDate())
                .personnel(reservation.getPersonnel())
                .totalPrice(reservation.getTotalPrice())
                .nights(nights)
                .status(reservation.getStatus())

                // 숙소 정보
                .accommodationId(reservation.getAccommodation().getAccommodationId())
                .accommodationName(reservation.getAccommodation().getName())
                .accommodationAddress(reservation.getAccommodation().getAddress())
                .checkInTime(reservation.getAccommodation().getCheckInTime())
                .checkOutTime(reservation.getAccommodation().getCheckOutTime())

                // 호스트 정보
                .hostName(reservation.getAccommodation().getMember().getName())
                .hostEmail(reservation.getAccommodation().getMember().getEmail())
                .hostPhone(reservation.getAccommodation().getMember().getPhone())

                // 게스트 정보
                .guestName(reservation.getMember().getName())
                .guestEmail(reservation.getMember().getEmail())
                .guestPhone(reservation.getMember().getPhone())

                .build();
    }

}

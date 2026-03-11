package com.binbang.backend.reservation.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCreateRequest {

    @NotNull(message="숙소 ID는 필수임")
    private Long accommodationId;

    @NotNull(message="체크인 날짜는 필수입니다.")
    @FutureOrPresent(message = "오늘 이후여햐 합니다.")
    private LocalDate checkInDate;

    @NotNull(message="체크아웃 날짜는 필수입니다.")
    @Future(message = "오늘 이후여햐 합니다.")
    private LocalDate checkOutDate;

    /**
     * 투숙 인원 (선택사항)
     * - 향후 인원별 가격 책정 등에 활용 가능
     */
    private Integer guestCount;
}

package com.binbang.backend.reservation.dto.request;

import jakarta.validation.constraints.*;
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

    @NotNull(message = "투숙 인원은 필수입니다")
    @Min(value = 1, message = "투숙 인원은 최소 1명입니다")
    @Max(value = 20, message = "투숙 인원은 최대 20명입니다")
    private Integer guestCount;
}

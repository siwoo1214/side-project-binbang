package com.binbang.backend.reservation.exception;

import com.binbang.backend.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public class ReservationAlreadyCancelledException extends CustomException {
    public ReservationAlreadyCancelledException(Long reservationId) {
        super(HttpStatus.CONFLICT,
                "이미 취소된 예약입니다: " + reservationId);
    }
}

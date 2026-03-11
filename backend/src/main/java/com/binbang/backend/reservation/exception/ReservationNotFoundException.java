package com.binbang.backend.reservation.exception;

import com.binbang.backend.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public class ReservationNotFoundException extends CustomException {
    public ReservationNotFoundException(Long reservationId){
        super(HttpStatus.NOT_FOUND,reservationId+"번 예약을 찾을 수 없습니다.");
    }
}

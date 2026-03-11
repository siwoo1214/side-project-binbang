package com.binbang.backend.reservation.exception;

import com.binbang.backend.global.exception.CustomException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;

public class InvalidReservationDateException extends CustomException {
    public InvalidReservationDateException(LocalDate checkIn, LocalDate checkOut){
        super(HttpStatus.CONFLICT,
                String.format("체크인과 체크아웃 날짜를 확인해주세요. 체크인:%s 체크아웃:%s",checkIn,checkOut));
    }

    public InvalidReservationDateException(LocalDate checkIn){
        super(HttpStatus.CONFLICT,
                String.format("과거 날짜로 체크인할 수 없습니다. 체크인: %s",checkIn));
    }
}

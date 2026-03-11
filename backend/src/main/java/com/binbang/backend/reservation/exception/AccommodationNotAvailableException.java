package com.binbang.backend.reservation.exception;

import com.binbang.backend.global.exception.CustomException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;

public class AccommodationNotAvailableException extends CustomException {
    public AccommodationNotAvailableException(Long accommodationId, LocalDate checkIn, LocalDate checkOut){
        super(HttpStatus.CONFLICT,
                String.format("해당 기간(%s ~ %s)에 숙소(ID: %d)가 예약되어 있습니다.\n다른 날짜를 선택해 주세요.",
                        checkIn, checkOut, accommodationId));
    }

    public AccommodationNotAvailableException(){
        super(HttpStatus.CONFLICT,
                "숙소가 이미 예약되어있습니다.");
    }
}

package com.binbang.backend.accommodation.exception;

import com.binbang.backend.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public class AccommodationNotFoundException extends CustomException {

    // 숙소가 삭제된 후 위시리스트 작업을 시도할 때
    public AccommodationNotFoundException(Long accommodationId) {
        super(HttpStatus.NOT_FOUND, "존재하지 않는 숙소입니다: " + accommodationId);
    }
}

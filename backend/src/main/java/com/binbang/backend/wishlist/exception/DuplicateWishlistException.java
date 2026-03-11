package com.binbang.backend.wishlist.exception;

import com.binbang.backend.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public class DuplicateWishlistException extends CustomException {

    // 이미 위시리스트에 추가된 숙소를 또 추가하려고 할 떄
    // 근데 딱히 필요없을거같은게 하트를 눌러서 추가 삭제 하게 만들면
    // 이러한 예외는 없어도 상관없으듯
    public DuplicateWishlistException(Long accommodationId) {
        super(HttpStatus.CONFLICT,
                "이미 위시리스트에 추가된 숙소입니다 : "+accommodationId);
    }

}

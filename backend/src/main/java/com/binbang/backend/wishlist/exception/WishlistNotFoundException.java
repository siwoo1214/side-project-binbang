package com.binbang.backend.wishlist.exception;

import com.binbang.backend.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public class WishlistNotFoundException extends CustomException {

    // 사용자가 삭제하려는 위시리스트가 없을 때
    public WishlistNotFoundException(Long listId) {
        super(HttpStatus.NOT_FOUND, "존재하지 않는 위시리스트입니다: " + listId);
    }

    // 이미 삭제된 위시리스트를 조회할 떄
    public WishlistNotFoundException(Long memberId, Long accommodationId){
        super(HttpStatus.NOT_FOUND,
                String.format("위시리스트를 찾을 수 없습니다. (ID : %d, 숛소 : %d)",memberId,accommodationId));
    }

}

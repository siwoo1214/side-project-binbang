package com.binbang.backend.wishlist.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 위시리스트 토글(추가/삭제) 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistToggleResponse {
    private boolean isWishlisted;  // true: 추가됨, false: 삭제됨
    private String message;

    // 이름 포함
    public static WishlistToggleResponse added(String accommodationName) {
        return WishlistToggleResponse.builder()
                .isWishlisted(true)
                .message(accommodationName+"이(가) 위시리스트에 추가되었습니다")
                .build();
    }

    // 이름 포함
    public static WishlistToggleResponse removed(String accommodationName) {
        return WishlistToggleResponse.builder()
                .isWishlisted(false)
                .message(accommodationName + "이(가) 위시리스트에서 제거되었습니다")
                .build();
    }
}

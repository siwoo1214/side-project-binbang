package com.binbang.backend.wishlist.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 위시리스트 토글(추가/삭제) 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WishlistToggleRequest {
    @NotNull(message = "클릭했는데 사용가능한 accommodation id가 들어오지 않읐스")
    private Long accommodationId;
}

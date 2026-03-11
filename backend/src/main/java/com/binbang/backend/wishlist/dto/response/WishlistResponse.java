package com.binbang.backend.wishlist.dto.response;

import com.binbang.backend.wishlist.entity.WishList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistResponse {

    private Long listId;  // 위시리스트 ID
    private Long accommodationId;  // 숙소 ID
    private String accommodationName;  // 숙소 이름
    private Long price;  // 가격
    private String address;  // 주소
    private String categoryName;  // 카테고리명
    private LocalTime checkInTime;  // 체크인 시간
    private LocalTime checkOutTime;  // 체크아웃 시간
    // private String imageUrl; 이거는 나중에 이미지 주소 넣을거임

    /**
     * WishList 엔티티로부터 DTO 생성
     */
    public static WishlistResponse from(WishList wishList) {
        return WishlistResponse.builder()
                .listId(wishList.getListId())
                .accommodationId(wishList.getAccommodation().getAccommodationId())
                .accommodationName(wishList.getAccommodation().getName())
                .price(wishList.getAccommodation().getPrice())
                .address(wishList.getAccommodation().getAddress())
                .categoryName(wishList.getAccommodation().getCategory().getName())
                .checkInTime(wishList.getAccommodation().getCheckInTime())
                .checkOutTime(wishList.getAccommodation().getCheckOutTime())
                .build();
    }

}

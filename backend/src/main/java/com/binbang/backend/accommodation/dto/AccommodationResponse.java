package com.binbang.backend.accommodation.dto;

import com.binbang.backend.accommodation.entity.Accommodation;
import com.binbang.backend.category.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccommodationResponse {
    private Long accommodationId;  // 이미지 업로드 등 후속 작업에 필요
    private String name;
    private Long price;
    private String description;
    private String address;
    private Double latitude;
    private Double longitude;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private String categoryName;

    public static AccommodationResponse from(Accommodation accommodation){
        return AccommodationResponse.builder()
                .accommodationId(accommodation.getAccommodationId())
                .name(accommodation.getName())
                .price(accommodation.getPrice())
                .description(accommodation.getDescription())
                .address(accommodation.getAddress())
                .latitude(accommodation.getLatitude())
                .longitude(accommodation.getLongitude())
                .checkInTime(accommodation.getCheckInTime())
                .checkOutTime(accommodation.getCheckOutTime())
                .categoryName(accommodation.getCategory().getName())
                .build();
    }
}

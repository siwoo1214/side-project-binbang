package com.binbang.backend.accommodation.dto;

import com.binbang.backend.accommodation.entity.Accommodation;
import com.binbang.backend.accommodation.entity.AccommodationFacility;
import com.binbang.backend.accommodation.entity.AccommodationImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccommodationDetailResponse {

    private Long accommodationId;
    private String name;
    private Long price;
    private String description;
    private String address;
    private Double latitude;
    private Double longitude;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private String categoryName;
    private String regionName;
    private String hostName;

    // 이미지 URL 목록 (sortOrder 순)
    private List<String> imageUrls;

    // 시설 정보
    private Integer bedrooms;
    private Integer bathrooms;
    private Integer beds;
    private boolean petAllowed;
    private boolean parkingAvailable;
    private boolean hasBbq;
    private boolean hasWifi;

    public static AccommodationDetailResponse from(Accommodation accommodation) {
        // 이미지 URL 목록 (sortOrder 오름차순)
        List<String> imageUrls = accommodation.getImages() == null ? List.of() :
                accommodation.getImages().stream()
                        .sorted((a, b) -> a.getSortOrder() - b.getSortOrder())
                        .map(AccommodationImage::getImageUrl)
                        .collect(Collectors.toList());

        // 시설 정보 (없을 수도 있음)
        AccommodationFacility facility = accommodation.getFacility();

        return AccommodationDetailResponse.builder()
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
                .regionName(accommodation.getRegion().getName())
                .hostName(accommodation.getMember().getName())
                .imageUrls(imageUrls)
                // 시설 정보 (null이면 기본값)
                .bedrooms(facility != null ? facility.getBedrooms() : null)
                .bathrooms(facility != null ? facility.getBathrooms() : null)
                .beds(facility != null ? facility.getBeds() : null)
                .petAllowed(facility != null && facility.isPetAllowed())
                .parkingAvailable(facility != null && facility.isParkingAvailable())
                .hasBbq(facility != null && facility.isHasBbq())
                .hasWifi(facility != null && facility.isHasWifi())
                .build();
    }
}

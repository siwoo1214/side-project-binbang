package com.binbang.backend.global.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @description ES를 사용하기 위한 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccommodationIndexMessage implements Serializable {

    private Long accommodationId;
    private String name;
    private String description;
    private String address;
    private Long categoryId;
    private String categoryName;
    private Long regionId;
    private String regionName;
    private Long price;
    private Integer bedrooms;
    private Integer bathrooms;
    private Integer beds;
    private boolean petAllowed;
    private boolean parkingAvailable;
    private boolean hasBbq;
    private boolean hasWifi;
    private String status;
    private LocalDateTime createdAt;
}
package com.binbang.backend.accommodation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccommodationFacilityDto {
    @NotNull(message = "침실 개수를 입력하세요.")
    private Integer bedrooms;

    @NotNull(message = "욕실 개수를 입력하세요.")
    private Integer bathrooms;

    @NotNull(message = "침대 개수를 입력하세요.")
    private Integer beds;

    private boolean petAllowed;

    private boolean parkingAvailable;

    private boolean hasBbq;

    private boolean hasWifi;
}

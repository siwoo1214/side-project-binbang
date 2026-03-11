package com.binbang.backend.accommodation.dto;

import jakarta.persistence.Column;
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
public class AccommodationRegisterDto {

    @NotBlank(message = "숙소명을 입력하세요.")
    private String name;

    @NotNull(message = "가격을 입력하세요.")
    private Long price;

    @NotBlank(message = "어떤 숙소인지 소개해주세요.")
    private String description;

    @NotBlank(message = "주소를 입력하세요.")
    private String address;

    private Double latitude;

    private Double longitude;

    @NotNull(message = "체크인 시간을 입력하세요.")
    private LocalTime checkInTime;

    @NotNull(message = "체크아웃 시간을 입력하세요.")
    private LocalTime checkOutTime;

    @NotNull(message = "카테고리를 등록해주세요.")
    private Long categoryId;

    @NotNull(message = "시설 정보를 입력하세요.")
    @Valid
    private AccommodationFacilityDto facility;

    @NotNull(message = "정책 정보를 입력하세요.")
    @Valid
    private AccommodationPolicyDto policy;

    @NotBlank(message = "지역 정보를 입력하세요.")
    private String regionName;
}

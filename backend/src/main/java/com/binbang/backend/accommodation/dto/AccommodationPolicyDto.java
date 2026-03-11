package com.binbang.backend.accommodation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccommodationPolicyDto {

    @NotBlank(message = "환불 및 취소 정책을 입력하세요.")
    private String refundPolicy;

    @NotBlank(message = "숙소 이용 규칙을 입력하세요.")
    private String houseRules;

    @NotNull(message = "애완동물 가능 여부를 선택하세요.")
    private Boolean petAllowed;

    @NotNull(message = "주차 가능 여부를 선택하세요.")
    private Boolean parkingAvailable;

    @NotNull(message = "최대 인원을 입력하세요.")
    @Min(value = 1, message = "최대 인원은 1명 이상이어야 합니다.")
    private Integer maxGuests;

    @NotNull(message = "추가 인원 요금을 입력하세요.")
    @Min(value = 0, message = "추가 인원 요금은 0원 이상이어야 합니다.")
    private Long additionalGuestFee;
}

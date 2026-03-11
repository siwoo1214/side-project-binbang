package com.binbang.backend.address.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private String addressName;    // 전체 주소
    private Double latitude;       // 위도 (Double)
    private Double longitude;      // 경도 (Double)
    private String regionName;     // 구/군 이름 (필터링용)
}

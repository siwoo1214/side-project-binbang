package com.binbang.backend.address.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoAddress {
    @JsonProperty("region_1depth_name")
    private String region1DepthName;

    @JsonProperty("region_2depth_name")
    private String region2DepthName;
}

package com.binbang.backend.address.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class KakaoDocument {
    @JsonProperty("address_name")
    private String addressName;

    @JsonProperty("address")
    private KakaoAddress address;

    private String x;

    private String y;
}


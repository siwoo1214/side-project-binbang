package com.binbang.backend.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private Long memberId;      // 채팅 등 프론트에서 나를 식별할 때 필요
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;

    public static AuthResponse of(Long memberId, String accessToken, String refreshToken, Long expiresIn) {
        return AuthResponse.builder()
                .memberId(memberId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .build();
    }
}

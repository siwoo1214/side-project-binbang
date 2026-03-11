package com.binbang.backend.global.security.oauth2;

public interface OAuth2UserInfo {
    String getProviderId();  // Google: "123456789", Kakao: "987654321"
    String getProvider();  // "google" or "kakao"
    String getEmail();
    String getName();
}

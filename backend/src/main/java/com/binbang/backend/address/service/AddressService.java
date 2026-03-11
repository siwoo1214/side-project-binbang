package com.binbang.backend.address.service;

import com.binbang.backend.address.dto.AddressResponse;
import com.binbang.backend.address.dto.KakaoAddressResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final RestTemplate restTemplate; //HTTP 요청용

    @Value("${kakao.rest-api-key}")
    private String kakaoApiKey;

    public List<AddressResponse> searchAddress(String query){

        //1. 카카오 API URL 구성
        String url = "https://dapi.kakao.com/v2/local/search/address.json?query=" + query;

        //2. 헤더 설정(Authorization 필요)
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        //3. 카카오 API 호출
        ResponseEntity<KakaoAddressResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                KakaoAddressResponse.class
        );

        //4. 응답을 우리 DTO로 변환
        KakaoAddressResponse kakaoResponse = response.getBody();
        return kakaoResponse.getDocuments().stream()
                .map(document -> AddressResponse.builder()
                        .addressName(document.getAddressName())
                        .latitude(Double.parseDouble(document.getY()))
                        .longitude(Double.parseDouble(document.getX()))
                        .regionName(document.getAddress().getRegion2DepthName())
                        .build())
                .collect(Collectors.toList());
    }
}

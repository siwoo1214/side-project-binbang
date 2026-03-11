package com.binbang.backend.wishlist.controller;

import com.binbang.backend.wishlist.dto.request.WishlistToggleRequest;
import com.binbang.backend.wishlist.dto.response.WishlistResponse;
import com.binbang.backend.wishlist.dto.response.WishlistToggleResponse;
import com.binbang.backend.wishlist.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping("/health")
    public String healthCheck(){
        return "strong strong";
    }

    /**
     * 위시리스트 토글 API (추가/삭제)
     * POST /api/wishlist/toggle
     *
     * @param email 현재 로그인한 사용자 이메일 (JWT에서 추출)
     * @param request 숙소 ID
     * @return 토글 결과 (추가됨/삭제됨)
     */
    @PostMapping("/toggle")
    public ResponseEntity<WishlistToggleResponse> toggleWishlist(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody WishlistToggleRequest request
            ){
        WishlistToggleResponse response = wishlistService.toggleWishlist(
                email, request.getAccommodationId()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 내 위시리스트 조회 API
     * GET /api/wishlist
     *
     * @param email 현재 로그인한 사용자 이메일 (JWT에서 추출)
     * @return 위시리스트 목록
     */
    @GetMapping
    public ResponseEntity<List<WishlistResponse>> getMyWishlists(
            @AuthenticationPrincipal String email
    ){
        List<WishlistResponse> responses = wishlistService.getMyWishlists(email);
        return ResponseEntity.ok(responses);
    }

    /**
     * 내 위시리스트 개수 조회 API
     * GET /api/wishlist/count
     *
     * @param email 현재 로그인한 사용자 이메일 (JWT에서 추출)
     * @return 위시리스트 개수
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getWishlistCount(
            @AuthenticationPrincipal String email
    ) {
        log.info("위시리스트 개수 조회: email={}", email);

        long count = wishlistService.getWishlistCount(email);

        Map<String, Long> response = new HashMap<>();
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

}

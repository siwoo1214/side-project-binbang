package com.binbang.backend.wishlist.service;

import com.binbang.backend.accommodation.entity.Accommodation;
import com.binbang.backend.accommodation.exception.AccommodationNotFoundException;
import com.binbang.backend.accommodation.repository.AccommodationRepository;
import com.binbang.backend.member.entity.Member;
import com.binbang.backend.member.exception.MemberNotFoundException;
import com.binbang.backend.member.repository.MemberRepository;
import com.binbang.backend.wishlist.dto.response.WishlistResponse;
import com.binbang.backend.wishlist.dto.response.WishlistToggleResponse;
import com.binbang.backend.wishlist.entity.WishList;
import com.binbang.backend.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 위시리스트 비즈니스 로직
 * - 위시리스트 토글 (추가/삭제)
 * - 내 위시리스트 조회
 * 
 * email 기반으로 동작 (Spring Security @AuthenticationPrincipal과 일관성 유지)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final MemberRepository memberRepository;
    private final AccommodationRepository accommodationRepository;

    /**
     * 위시리스트 토글 (추가/삭제)
     * - 이미 위시리스트에 있으면 삭제
     * - 없으면 추가
     * 
     * @param email 로그인한 회원 이메일 (JWT 토큰에서 추출)
     * @param accommodationId 숙소 ID
     * @return 토글 결과 (추가됨/삭제됨)
     */
    @Transactional
    public WishlistToggleResponse toggleWishlist(String email, Long accommodationId) {
        Long memberId = getMemberIdByEmail(email);
        return toggleWishlistInternal(memberId, accommodationId);
    }

    /**
     * 내 위시리스트 조회
     * 
     * @param email 로그인한 회원 이메일 (JWT 토큰에서 추출)
     * @return 위시리스트 목록
     */
    @Transactional(readOnly = true)
    public List<WishlistResponse> getMyWishlists(String email) {
        Long memberId = getMemberIdByEmail(email);
        return getMyWishlistsInternal(memberId);
    }

    /**
     * 특정 숙소가 위시리스트에 있는지 확인
     * 
     * @param email 로그인한 회원 이메일 (JWT 토큰에서 추출)
     * @param accommodationId 숙소 ID
     * @return 위시리스트에 있으면 true
     */
    @Transactional(readOnly = true)
    public boolean isWishlisted(String email, Long accommodationId) {
        Long memberId = getMemberIdByEmail(email);
        boolean exists = wishlistRepository
                .existsByMember_MemberIdAndAccommodation_AccommodationId(memberId, accommodationId);
        
        log.info("위시리스트 존재 확인: email={}, accommodationId={}, exists={}", 
                 email, accommodationId, exists);
        return exists;
    }

    /**
     * 내 위시리스트 개수 조회
     * 
     * @param email 로그인한 회원 이메일 (JWT 토큰에서 추출)
     * @return 위시리스트 개수
     */
    @Transactional(readOnly = true)
    public long getWishlistCount(String email) {
        Long memberId = getMemberIdByEmail(email);
        long count = wishlistRepository.countByMember_MemberId(memberId);
        log.info("위시리스트 개수 조회: email={}, count={}", email, count);
        return count;
    }

    // ===== Private Helper Methods =====
    
    /**
     * email로 memberId 조회 (헬퍼 메서드)
     * 
     * @param email 회원 이메일
     * @return 회원 ID
     * @throws MemberNotFoundException 회원을 찾을 수 없을 때
     */
    private Long getMemberIdByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException(email))
                .getMemberId();
    }

    /**
     * 위시리스트 토글 내부 로직 (memberId 기반)
     */
    private WishlistToggleResponse toggleWishlistInternal(Long memberId, Long accommodationId) {
        // 1. 회원 존재 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("회원을 찾을 수 없습니다"));

        // 2. 숙소 존재 확인
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new AccommodationNotFoundException(accommodationId));

        // 3. 기존 위시리스트 조회
        Optional<WishList> existingWishlist = wishlistRepository
                .findByMember_MemberIdAndAccommodation_AccommodationId(memberId, accommodationId);

        // 4. 토글 처리
        if (existingWishlist.isPresent()) {
            // 이미 있으면 삭제
            wishlistRepository.delete(existingWishlist.get());
            log.info("위시리스트 삭제: memberId={}, accommodationId={}", memberId, accommodationId);
            return WishlistToggleResponse.removed(accommodation.getName());
        } else {
            // 없으면 추가
            WishList wishList = new WishList(null, member, accommodation);
            wishlistRepository.save(wishList);
            log.info("위시리스트 추가: memberId={}, accommodationId={}", memberId, accommodationId);
            return WishlistToggleResponse.added(accommodation.getName());
        }
    }

    /**
     * 위시리스트 조회 내부 로직 (memberId 기반)
     */
    private List<WishlistResponse> getMyWishlistsInternal(Long memberId) {
        // 1. 회원 존재 확인
        if (!memberRepository.existsById(memberId)) {
            throw new MemberNotFoundException("회원을 찾을 수 없습니다");
        }

        // 2. 위시리스트 조회
        List<WishList> wishlists = wishlistRepository.findByMember_MemberId(memberId);

        // 3. DTO로 변환
        List<WishlistResponse> responses = wishlists.stream()
                .map(WishlistResponse::from)
                .collect(Collectors.toList());

        log.info("위시리스트 조회: memberId={}, count={}", memberId, responses.size());
        return responses;
    }
}

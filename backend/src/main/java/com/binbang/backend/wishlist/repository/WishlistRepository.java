package com.binbang.backend.wishlist.repository;

import com.binbang.backend.wishlist.entity.WishList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 이거 메소드명들을 내가 바꾸려고 했는데
 * JPA의 메소드명 법칙이 있어서
 * 바꾸면 내가 직접 구현해야 해서
 * 안바꾸는게 낫다고 그랬음
 */

@Repository
public interface WishlistRepository extends JpaRepository<WishList, Long> {

    // 특정 회원의 모든 위시리스트 조회
    List<WishList> findByMember_MemberId(Long memberId);
    // 이는 다음 JPQL과 같음:
    // SELECT w FROM WishList w WHERE w.member.memberId = :memberId

    // 특정 회원의 특정 숙소 위시리스트 존재 여부 확인
    boolean existsByMember_MemberIdAndAccommodation_AccommodationId(Long memberId, Long accommodationId);

    // 특정 회원의 특정 숙소 위시리스트 조회
    Optional<WishList> findByMember_MemberIdAndAccommodation_AccommodationId(Long memberId, Long accommodationId);

    // 특정 회원의 위시리스트 개수 조회
    long countByMember_MemberId(Long memberId);

}

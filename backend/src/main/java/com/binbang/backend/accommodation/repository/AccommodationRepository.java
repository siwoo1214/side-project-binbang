package com.binbang.backend.accommodation.repository;

import com.binbang.backend.accommodation.entity.Accommodation;
import com.binbang.backend.accommodation.entity.AccommodationStatus;
import com.binbang.backend.reservation.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccommodationRepository extends JpaRepository<Accommodation, Long>, JpaSpecificationExecutor<Accommodation> {

    Page<Accommodation> findAll(Pageable pageable);

    Page<Accommodation> findByCategory_CategoryId(Long categoryId, Pageable pageable);

    // 특정 회원이 등록한 숙소 목록 조회 (최신순)
    List<Accommodation> findByMember_MemberIdOrderByCreatedAtDesc(Long memberId);

    // JPA 경로(LIKE·대조군·필터): Specification 페이징 조회 시 category, region 함께 조회
    @Override
    @EntityGraph(attributePaths = {"category", "region"})
    Page<Accommodation> findAll(Specification<Accommodation> spec, Pageable pageable);

    // ES 경로: ID 목록으로 재조회 시 category, region 함께 조회
    @EntityGraph(attributePaths = {"category", "region"})
    List<Accommodation> findByAccommodationIdIn(List<Long> accommodationIds);

    // 벌크 재색인: ID 기준 (pk)키셋 페이징, category·region 함께 조회
    @EntityGraph(attributePaths = {"category", "region"})
    List<Accommodation> findByAccommodationIdGreaterThanOrderByAccommodationIdAsc(Long lastId, Pageable pageable);
}

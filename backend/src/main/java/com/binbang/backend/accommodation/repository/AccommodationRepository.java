package com.binbang.backend.accommodation.repository;

import com.binbang.backend.accommodation.entity.Accommodation;
import com.binbang.backend.accommodation.entity.AccommodationStatus;
import com.binbang.backend.reservation.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccommodationRepository extends JpaRepository<Accommodation, Long>, JpaSpecificationExecutor<Accommodation> {

    Page<Accommodation> findAll(Pageable pageable);

    Page<Accommodation> findByCategory_CategoryId(Long categoryId, Pageable pageable);

    // 특정 회원이 등록한 숙소 목록 조회 (최신순)
    List<Accommodation> findByMember_MemberIdOrderByCreatedAtDesc(Long memberId);

//    @Modifying
//    @Query("update Accommodation a set a.status = :status where a.id = :id")
//    int updateStatusById(@Param("id") Long id,
//                         @Param("status") AccommodationStatus status);
}

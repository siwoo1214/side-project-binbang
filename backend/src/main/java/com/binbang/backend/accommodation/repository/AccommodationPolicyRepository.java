package com.binbang.backend.accommodation.repository;

import com.binbang.backend.accommodation.entity.AccommodationPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccommodationPolicyRepository extends JpaRepository<AccommodationPolicy, Long> {
}

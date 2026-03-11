package com.binbang.backend.category.repository;

import com.binbang.backend.category.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {
    List<Region> findByDepth(int depth);

    List<Region> findByParent(Region parent);

    Optional<Region> findByName(String name);
}

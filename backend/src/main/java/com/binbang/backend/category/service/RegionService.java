package com.binbang.backend.category.service;

import com.binbang.backend.category.dto.RegionResponse;
import com.binbang.backend.category.entity.Region;
import com.binbang.backend.category.exception.RegionNotFoundException;
import com.binbang.backend.category.repository.RegionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class RegionService {
    private final RegionRepository regionRepository;

    public List<RegionResponse> getTopRegions() {
        List<Region> topRegions = regionRepository.findByDepth(1);

        return topRegions.stream()
                .map(RegionResponse::from)
                .toList();
    }

    public List<RegionResponse> getChildRegions(Long parentId){
        Region region = regionRepository.findById(parentId)
                .orElseThrow(() -> new RegionNotFoundException(parentId));

        List<Region> childRegions = regionRepository.findByParent(region);

        return childRegions.stream()
                .map(RegionResponse::from)
                .toList();
    }
}

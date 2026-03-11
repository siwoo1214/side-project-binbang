package com.binbang.backend.category.controller;

import com.binbang.backend.category.dto.RegionResponse;
import com.binbang.backend.category.service.RegionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/regions")
@AllArgsConstructor
public class RegionController {

    private final RegionService regionService;

    @GetMapping("/top")
    public ResponseEntity<List<RegionResponse>> getTopRegions(){
        List<RegionResponse> topRegions = regionService.getTopRegions();

        return ResponseEntity.ok(topRegions);
    }

    @GetMapping("/{regionId}/children")
    public ResponseEntity<List<RegionResponse>> getChildRegions(@PathVariable("regionId") Long regionId){
        return ResponseEntity.ok(regionService.getChildRegions(regionId));
    }
}

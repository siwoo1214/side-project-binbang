package com.binbang.backend.accommodation.controller;

import com.binbang.backend.accommodation.dto.AccommodationDetailResponse;
import com.binbang.backend.accommodation.dto.AccommodationListResponse;
import com.binbang.backend.accommodation.dto.AccommodationRegisterDto;
import com.binbang.backend.accommodation.dto.AccommodationResponse;
import com.binbang.backend.accommodation.service.AccommodationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/api/accommodation")
@RequiredArgsConstructor
public class AccommodationController {

    private final AccommodationService accommodationService;

    // 숙소 상세 조회 (비로그인도 가능, SecurityConfig에서 GET /api/accommodation/** 퍼블릭 허용)
    @GetMapping("/{id}")
    public ResponseEntity<AccommodationDetailResponse> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(accommodationService.getDetail(id));
    }

    @PostMapping("/register")
    public ResponseEntity<AccommodationResponse> register(@Valid @RequestBody AccommodationRegisterDto dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(accommodationService.register(dto));
    }

    @GetMapping("/list")
    public ResponseEntity<Page<AccommodationListResponse>> getList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer minBedrooms,
            @RequestParam(required = false) Integer minBathrooms,
            @RequestParam(required = false) Integer minBeds,
            @RequestParam(required = false) Boolean petAllowed,
            @RequestParam(required = false) Boolean parkingAvailable,
            @RequestParam(required = false) Boolean hasBbq,
            @RequestParam(required = false) Boolean hasWifi,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long regionId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable){
        Page<AccommodationListResponse> result = accommodationService.getList(
                categoryId, minBedrooms, minBathrooms, minBeds,
                petAllowed, parkingAvailable, hasBbq, hasWifi,
                keyword, regionId,pageable);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<String> uploadImages(
            @PathVariable Long id,
            @RequestParam("images") List<MultipartFile> images
    ) throws IOException {
        accommodationService.uploadImages(id, images);
        return ResponseEntity.ok("이미지 업로드 완료");
    }

    // 내가 등록한 숙소 목록 조회 (로그인 필요)
    @GetMapping("/my")
    public ResponseEntity<List<AccommodationListResponse>> getMyAccommodations() {
        return ResponseEntity.ok(accommodationService.getMyAccommodations());
    }
}

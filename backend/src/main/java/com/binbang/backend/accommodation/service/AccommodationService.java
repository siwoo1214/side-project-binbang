package com.binbang.backend.accommodation.service;

import com.binbang.backend.accommodation.dto.AccommodationDetailResponse;
import com.binbang.backend.accommodation.dto.AccommodationFacilityDto;
import com.binbang.backend.accommodation.dto.AccommodationListResponse;
import com.binbang.backend.accommodation.entity.AccommodationFacility;
import com.binbang.backend.accommodation.entity.AccommodationImage;
import com.binbang.backend.accommodation.entity.AccommodationPolicy;
import com.binbang.backend.accommodation.exception.AccommodationNotFoundException;
import com.binbang.backend.accommodation.exception.CategoryNotFoundException;
import com.binbang.backend.accommodation.dto.AccommodationRegisterDto;
import com.binbang.backend.accommodation.dto.AccommodationResponse;
import com.binbang.backend.accommodation.entity.Accommodation;
import com.binbang.backend.accommodation.repository.AccommodationFacilityRepository;
import com.binbang.backend.accommodation.repository.AccommodationImageRepository;
import com.binbang.backend.accommodation.repository.AccommodationPolicyRepository;
import com.binbang.backend.accommodation.repository.AccommodationRepository;
import com.binbang.backend.accommodation.specification.AccommodationSpecification;
import com.binbang.backend.category.entity.Category;
import com.binbang.backend.category.entity.Region;
import com.binbang.backend.category.exception.RegionNotFoundException;
import com.binbang.backend.category.repository.CategoryRepository;
import com.binbang.backend.category.repository.RegionRepository;
import com.binbang.backend.global.exception.CustomException;
import com.binbang.backend.global.service.S3Service;
import com.binbang.backend.member.entity.Member;
import com.binbang.backend.member.exception.MemberNotFoundException;
import com.binbang.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final AccommodationFacilityRepository facilityRepository;
    private final AccommodationPolicyRepository policyRepository;
    private final AccommodationImageRepository accommodationImageRepository;
    private final RegionRepository regionRepository;
    private final ObjectMapper objectMapper;
    private final S3Service s3Service;

    public Member getCurrentMember(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException(email));
    }

    @Transactional
    public AccommodationResponse register(AccommodationRegisterDto dto) {
        Member member = getCurrentMember();

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(dto.getCategoryId()));

        Region region = regionRepository.findByName(dto.getRegionName())
                .orElseThrow(() -> new RegionNotFoundException(dto.getRegionName()));

        Accommodation accommodation = Accommodation.builder()
                .member(member)
                .name(dto.getName())
                .address(dto.getAddress())
                .price(dto.getPrice())
                .checkInTime(dto.getCheckInTime())
                .checkOutTime(dto.getCheckOutTime())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .description(dto.getDescription())
                .category(category)
                .region(region)
                .build();

        accommodationRepository.save(accommodation);

        AccommodationFacility facility = AccommodationFacility.builder()
                .accommodation(accommodation)
                .bedrooms(dto.getFacility().getBedrooms())
                .bathrooms(dto.getFacility().getBathrooms())
                .beds(dto.getFacility().getBeds())
                .petAllowed(dto.getFacility().isPetAllowed())
                .parkingAvailable(dto.getFacility().isParkingAvailable())
                .hasBbq(dto.getFacility().isHasBbq())
                .hasWifi(dto.getFacility().isHasWifi())
                .build();

        facilityRepository.save(facility);

        String policyJson;
        try {
            policyJson = objectMapper.writeValueAsString(dto.getPolicy());
        } catch (Exception e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "정책 정보 처리 중 오류가 발생했습니다.");
        }
        AccommodationPolicy policy = new AccommodationPolicy();
        policy.setAccommodation(accommodation);
        policy.setPolicies(policyJson);

        policyRepository.save(policy);

        return AccommodationResponse.from(accommodation);
    }

    @Transactional
    public void uploadImages(Long accommodationId, List<MultipartFile> images) throws IOException{
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(()-> new AccommodationNotFoundException(accommodationId));

        Member currentMember = getCurrentMember();

        if(!accommodation.getMember().getMemberId().equals(currentMember.getMemberId())){
            throw new CustomException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
        }

        for(int i = 0 ; i < images.size() ; i++){
            MultipartFile image = images.get(i);

            // S3에 업로드
            String imageUrl = s3Service.upLoadFile(image);

            //AccommodationImage 엔티티 생성
            AccommodationImage accommodationImage = new AccommodationImage();
            accommodationImage.setAccommodation(accommodation);
            accommodationImage.setImageUrl(imageUrl);
            accommodationImage.setSortOrder(i);

            //DB 저장
            accommodationImageRepository.save(accommodationImage);
        }
    }

    @Transactional(readOnly = true)
    public AccommodationDetailResponse getDetail(Long accommodationId) {
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new AccommodationNotFoundException(accommodationId));
        return AccommodationDetailResponse.from(accommodation);
    }

    @Transactional
    public Page<AccommodationListResponse> getList(
            Long categoryId,
            Integer minBedrooms,
            Integer minBathrooms,
            Integer minBeds,
            Boolean petAllowed,
            Boolean parkingAvailable,
            Boolean hasBbq,
            Boolean hasWifi,
            String keyword,
            Long regionId,
            Pageable pageable
    ){
        List<Long> regionIds = new ArrayList<>();

        if(regionId != null){
            Region region = regionRepository.findById(regionId)
                    .orElseThrow(() -> new RegionNotFoundException(regionId));

            if (region.getDepth() == 1){
                List<Region> children = regionRepository.findByParent(region);
                regionIds = children.stream()
                        .map(Region::getRegionId)
                        .collect(Collectors.toList());
            }else{
                regionIds.add(regionId);
            }
        }

        //Specification 조합
        Specification<Accommodation> spec = Specification
                .where(AccommodationSpecification.hasCategory(categoryId))
                .and(AccommodationSpecification.hasMinBedrooms(minBedrooms))
                .and(AccommodationSpecification.hasMinBathrooms(minBathrooms))
                .and(AccommodationSpecification.hasMinBeds(minBeds))
                .and(AccommodationSpecification.petAllowed(petAllowed))
                .and(AccommodationSpecification.parkingAvailable(parkingAvailable))
                .and(AccommodationSpecification.hasBbq(hasBbq))
                .and(AccommodationSpecification.hasWifi(hasWifi))
                .and(AccommodationSpecification.addressLike(keyword))
                .and(AccommodationSpecification.hasRegionIn(regionIds));

        //위에서 만든 조건으로 페이징처리하여 조회
        Page<Accommodation> accommodationPage = accommodationRepository.findAll(spec, pageable);

        // DTO 변환 (정적 팩토리 메서드로 썸네일, 지역명, 카테고리명 포함)
        return accommodationPage.map(AccommodationListResponse::from);
    }

    // 내가 등록한 숙소 목록 조회
    @Transactional(readOnly = true)
    public List<AccommodationListResponse> getMyAccommodations() {
        Member member = getCurrentMember();
        List<Accommodation> accommodations = accommodationRepository
                .findByMember_MemberIdOrderByCreatedAtDesc(member.getMemberId());
        return accommodations.stream()
                .map(AccommodationListResponse::from)
                .collect(Collectors.toList());
    }
}

package com.binbang.backend.accommodation.service;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.json.JsonData;
import com.binbang.backend.accommodation.document.AccommodationDocument;
import com.binbang.backend.accommodation.dto.AccommodationDetailResponse;
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
import com.binbang.backend.global.dto.AccommodationIndexMessage;
import com.binbang.backend.global.exception.CustomException;
import com.binbang.backend.global.service.MessageProducer;
import com.binbang.backend.global.service.S3Service;
import com.binbang.backend.member.entity.Member;
import com.binbang.backend.member.exception.MemberNotFoundException;
import com.binbang.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
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
    // es 사용하기 위한 의존성 주입
    private final ElasticsearchOperations elasticsearchOperations;
    private final MessageProducer messageProducer;

    // 검색 엔진 토글 (es / like) - final이 아니어야 @RequiredArgsConstructor 생성자에 포함되지 않음
    @Value("${search.engine:es}")
    private String searchEngine;

    @PostConstruct
    void logSearchEngine() {
        log.info("숙소 검색 엔진: {}", searchEngine);
    }

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

        messageProducer.sendAccommodationIndexMessage(
                AccommodationIndexMessage.builder()
                        .accommodationId(accommodation.getAccommodationId())
                        .name(accommodation.getName())
                        .description(accommodation.getDescription())
                        .address(accommodation.getAddress())
                        .categoryId(category.getCategoryId())
                        .categoryName(category.getName())
                        .regionId(region.getRegionId())
                        .regionName(region.getName())
                        .price(accommodation.getPrice())
                        .bedrooms(facility.getBedrooms())
                        .bathrooms(facility.getBathrooms())
                        .beds(facility.getBeds())
                        .petAllowed(facility.isPetAllowed())
                        .parkingAvailable(facility.isParkingAvailable())
                        .hasBbq(facility.isHasBbq())
                        .hasWifi(facility.isHasWifi())
                        .status(accommodation.getStatus().name())
                        .createdAt(accommodation.getCreatedAt())
                        .build()
        );

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
        AccommodationFacility facility = facilityRepository.findById(accommodationId).orElse(null);
        return AccommodationDetailResponse.from(accommodation, facility);
    }

//    @Transactional
//    public Page<AccommodationListResponse> getList(
//            Long categoryId,
//            Integer minBedrooms,
//            Integer minBathrooms,
//            Integer minBeds,
//            Boolean petAllowed,
//            Boolean parkingAvailable,
//            Boolean hasBbq,
//            Boolean hasWifi,
//            String keyword,
//            Long regionId,
//            Pageable pageable
//    ){
//        List<Long> regionIds = new ArrayList<>();
//
//        if(regionId != null){
//            Region region = regionRepository.findById(regionId)
//                    .orElseThrow(() -> new RegionNotFoundException(regionId));
//
//            if (region.getDepth() == 1){
//                List<Region> children = regionRepository.findByParent(region);
//                regionIds = children.stream()
//                        .map(Region::getRegionId)
//                        .collect(Collectors.toList());
//            }else{
//                regionIds.add(regionId);
//            }
//        }
//
//        //Specification 조합
//        Specification<Accommodation> spec = Specification
//                .where(AccommodationSpecification.hasCategory(categoryId))
//                .and(AccommodationSpecification.hasMinBedrooms(minBedrooms))
//                .and(AccommodationSpecification.hasMinBathrooms(minBathrooms))
//                .and(AccommodationSpecification.hasMinBeds(minBeds))
//                .and(AccommodationSpecification.petAllowed(petAllowed))
//                .and(AccommodationSpecification.parkingAvailable(parkingAvailable))
//                .and(AccommodationSpecification.hasBbq(hasBbq))
//                .and(AccommodationSpecification.hasWifi(hasWifi))
//                .and(AccommodationSpecification.addressLike(keyword))
//                .and(AccommodationSpecification.hasRegionIn(regionIds));
//
//        //위에서 만든 조건으로 페이징처리하여 조회
//        Page<Accommodation> accommodationPage = accommodationRepository.findAll(spec, pageable);
//
//        // DTO 변환 (정적 팩토리 메서드로 썸네일, 지역명, 카테고리명 포함)
//        return accommodationPage.map(AccommodationListResponse::from);
//    }

    // ES 활용한 숙소 목록 검색(필터링까지 포함해서)
//    @Transactional
//    public Page<AccommodationListResponse> getList(
//            Long categoryId, Integer minBedrooms, Integer minBathrooms, Integer minBeds,
//            Boolean petAllowed, Boolean parkingAvailable, Boolean hasBbq, Boolean hasWifi,
//            String keyword, Long regionId, Pageable pageable
//    ) {
//        List<Long> regionIds = resolveRegionIds(regionId);
//
//        if (keyword != null && !keyword.isBlank()) {
//            return searchByElasticsearch(categoryId, minBedrooms, minBathrooms, minBeds,
//                    petAllowed, parkingAvailable, hasBbq, hasWifi, keyword, regionIds, pageable);
//        }
//        return searchByJpa(categoryId, minBedrooms, minBathrooms, minBeds,
//                petAllowed, parkingAvailable, hasBbq, hasWifi, regionIds, pageable);
//    }

    // 디버깅 로그 삭제 + 분기 추가
    @Transactional(readOnly = true)
    public Page<AccommodationListResponse> getList(
            Long categoryId, Integer minBedrooms, Integer minBathrooms, Integer minBeds,
            Boolean petAllowed, Boolean parkingAvailable, Boolean hasBbq, Boolean hasWifi,
            String keyword, Long regionId, Pageable pageable
    ) {
        List<Long> regionIds = resolveRegionIds(regionId);

        // keyword 있음 + like 모드 → JPA LIKE 경로 (성능 비교용)
        if (keyword != null && !keyword.isBlank() && "like".equalsIgnoreCase(searchEngine)) {
            return searchByJpa(categoryId, minBedrooms, minBathrooms, minBeds,
                    petAllowed, parkingAvailable, hasBbq, hasWifi, keyword, regionIds, pageable);
        }

        // keyword 있음 + es 모드 → ES 경로
        if (keyword != null && !keyword.isBlank()) {
            return searchByElasticsearch(categoryId, minBedrooms, minBathrooms, minBeds,
                    petAllowed, parkingAvailable, hasBbq, hasWifi, keyword, regionIds, pageable);
        }

        // keyword 없음 → JPA 필터 경로
        return searchByJpa(categoryId, minBedrooms, minBathrooms, minBeds,
                petAllowed, parkingAvailable, hasBbq, hasWifi, null, regionIds, pageable);
    }

    // es 유틸리티 메소드들
    private List<Long> resolveRegionIds(Long regionId) {
        List<Long> regionIds = new ArrayList<>();
        if (regionId != null) {
            Region region = regionRepository.findById(regionId)
                    .orElseThrow(() -> new RegionNotFoundException(regionId));

            if (region.getDepth() == 1) {
                regionIds = regionRepository.findByParent(region).stream()
                        .map(Region::getRegionId)
                        .collect(Collectors.toList());
            } else {
                regionIds.add(regionId);
            }
        }
        return regionIds;
    }

    private Page<AccommodationListResponse> searchByElasticsearch(
            Long categoryId, Integer minBedrooms, Integer minBathrooms, Integer minBeds,
            Boolean petAllowed, Boolean parkingAvailable, Boolean hasBbq, Boolean hasWifi,
            String keyword, List<Long> regionIds, Pageable pageable
    ) {
        // Controller 기본 정렬(createdAt)을 떼어내고 페이지 번호·크기만 사용
        Pageable esPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());


        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> {

                    // 자유 검색어 → must (관련도 점수 계산에 반영됨)
                    b.must(m -> m.multiMatch(mm -> mm
                            .query(keyword)
                            .fields("name^3", "address^2", "regionName^2", "categoryName", "description")
                    ));

                    // 구조화된 조건 → filter (점수에 영향 없음, ES가 내부적으로 캐싱해줌)
                    if (categoryId != null) {
                        b.filter(f -> f.term(t -> t.field("categoryId").value(categoryId)));
                    }
                    if (!regionIds.isEmpty()) {
                        List<FieldValue> values = regionIds.stream().map(FieldValue::of).toList();
                        b.filter(f -> f.terms(t -> t.field("regionId").terms(ts -> ts.value(values))));
                    }
                    if (minBedrooms != null) {
                        b.filter(f -> f.range(r -> r.field("bedrooms").gte(JsonData.of(minBedrooms))));
                    }
                    if (minBathrooms != null) {
                        b.filter(f -> f.range(r -> r.field("bathrooms").gte(JsonData.of(minBathrooms))));
                    }
                    if (minBeds != null) {
                        b.filter(f -> f.range(r -> r.field("beds").gte(JsonData.of(minBeds))));
                    }
                    if (petAllowed != null) {
                        b.filter(f -> f.term(t -> t.field("petAllowed").value(petAllowed)));
                    }
                    if (parkingAvailable != null) {
                        b.filter(f -> f.term(t -> t.field("parkingAvailable").value(parkingAvailable)));
                    }
                    if (hasBbq != null) {
                        b.filter(f -> f.term(t -> t.field("hasBbq").value(hasBbq)));
                    }
                    if (hasWifi != null) {
                        b.filter(f -> f.term(t -> t.field("hasWifi").value(hasWifi)));
                    }

                    return b;
                }))
                // 1순위: 관련도 점수, 2순위: 최신순 (동점일 때 순서 고정)
                .withSort(s -> s.score(sc -> sc.order(SortOrder.Desc)))
                .withSort(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc)))
                .withPageable(esPageable)
                .build();

        SearchHits<AccommodationDocument> searchHits =
                elasticsearchOperations.search(query, AccommodationDocument.class);

        return mapToDtoPage(searchHits, pageable);
    }

    private Page<AccommodationListResponse> mapToDtoPage(
            SearchHits<AccommodationDocument> searchHits, Pageable pageable
    ) {
        List<Long> orderedIds = searchHits.getSearchHits().stream()
                .map(hit -> hit.getContent().getAccommodationId())
                .toList();

        if (orderedIds.isEmpty()) {
            return Page.empty(pageable);
        }

        Map<Long, Accommodation> accommodationMap = accommodationRepository
                .findByAccommodationIdIn(orderedIds).stream()
                .collect(Collectors.toMap(Accommodation::getAccommodationId, a -> a));

        List<AccommodationListResponse> content = orderedIds.stream()
                .map(accommodationMap::get)
                .filter(Objects::nonNull) // ES엔 있는데 DB에서 이미 삭제된 경우 방어
                .map(AccommodationListResponse::from)
                .toList();

        return new PageImpl<>(content, pageable, searchHits.getTotalHits());
    }

    private Page<AccommodationListResponse> searchByJpa(
            Long categoryId, Integer minBedrooms, Integer minBathrooms, Integer minBeds,
            Boolean petAllowed, Boolean parkingAvailable, Boolean hasBbq, Boolean hasWifi,
            String keyword, List<Long> regionIds, Pageable pageable
    ) {
        Specification<Accommodation> spec = Specification
                .where(AccommodationSpecification.hasCategory(categoryId))
                .and(AccommodationSpecification.hasMinBedrooms(minBedrooms))
                .and(AccommodationSpecification.hasMinBathrooms(minBathrooms))
                .and(AccommodationSpecification.hasMinBeds(minBeds))
                .and(AccommodationSpecification.petAllowed(petAllowed))
                .and(AccommodationSpecification.parkingAvailable(parkingAvailable))
                .and(AccommodationSpecification.hasBbq(hasBbq))
                .and(AccommodationSpecification.hasWifi(hasWifi))
                .and(AccommodationSpecification.keywordLike(keyword))
                .and(AccommodationSpecification.hasRegionIn(regionIds));

        Page<Accommodation> accommodationPage = accommodationRepository.findAll(spec, pageable);

        return accommodationPage.map(AccommodationListResponse::from);
    }
    // --------------------여기까지가 es 유틸리티 메소드

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
